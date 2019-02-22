package io.github.war501head.amazonparser.service;

import io.github.war501head.amazonparser.controller.ws.AmazonParserRestController;
import io.github.war501head.amazonparser.transfer.ParserResultsTO;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * The Business logic behind parsing amazon search results and putting out a score
 *
 * @author Sean K.
 * @see AmazonParserRestController for specific endpoints and their arguments
 */
@Service
public class AmazonParserService {

    private final Logger logger = LoggerFactory.getLogger(AmazonParserService.class);
    private final RestTemplate restTemplate;

    private static final String AMAZON_COMPLETION_URL = "https://completion.amazon.com/search/complete";

    @Autowired
    public AmazonParserService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Takes the keyword that has been passed into the microservice, breaks it down letter-by-letter, and parses the autocompletion API inside of Amazon
     * <p>
     * {@link ParserResultsTO#getScore()} is calculated by how early the keyword shows up. The more letters needed to find the key word, the less hot it is.
     * </p>
     *
     * @param keyword The keyword being parsed
     * @return The json transfer object
     */
    public ParserResultsTO getEstimate(String keyword) throws Exception {
        // Create our results object and set our keyword
        ParserResultsTO results = new ParserResultsTO();
        results.setKeyword(keyword);
        // This will be what each individual character of the keyword is "worth". For each character that it takes to find our keyword we're going to subtract
        // This amount from 100. E.g. a 10 letter word that takes us 5 characters to find in Amazon will be worth 50 points, with each letter being worth 10
        double individualScore = 100D / keyword.length();
        char[] charArray = keyword.toCharArray();
        StringBuilder builder = new StringBuilder();
        // Set up our stored uri variables which we will be using to parse the amazon API
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            // Build our URL with a dummy value for query
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(new URI(AMAZON_COMPLETION_URL))
                    // We're going to make sure that we're emulating the search ui here
                    .queryParam("client", "amazon-search-ui")
                    // "APS" is the keyword for "All Departments" inside of Amazon. Exactly what we need
                    .queryParam("search-alias", "aps")
                    // Set the market value to 1, likely the US. A
                    .queryParam("mkt", 1)
                    // Set a dummy value to be replaced later
                    .queryParam("q", "");

            // Compile our header entity for usage
            HttpEntity<?> entity = new HttpEntity<>(headers);

            // Our goal here will be to loop through by each character (as if we're typing this out). The more times that our keyword is found in this list, the hotter it is.
            for (int i = 0; i < charArray.length; i++) {
                builder.append(charArray[i]);
                // Update the prefix with the new value, substituting spaces for the + sign. (Maybe not needed?)
                uriBuilder.replaceQueryParam("q", builder.toString().replaceAll(" ", "+"));
                logger.debug("Contacting Amazon Completion Service: " + uriBuilder.toUriString());
                HttpEntity<String> serializedResponse = restTemplate.exchange(uriBuilder.toUriString(),
                        HttpMethod.GET,
                        entity,
                        String.class);

                if (serializedResponse != null) {
                    //Convert the serialized string into a JSONArray object, making it easier to parse
                    JSONArray jsonArray = new JSONArray(serializedResponse.getBody());
                    Status checkResult = isSuggestionPresentInArray(jsonArray, keyword);
                    // Check to see if we have a proper result, and if our suggestion is present in the array
                    if (checkResult.isPresent()) {
                        // Our suggestion has been found, now we calculate it's score, set the value, and return the result.
                        double score = (individualScore * (keyword.length() - i));
                        // If the result is the first result we'll keep the score, otherwise we'll remove half of a score
                        if (checkResult != Status.FIRST) {
                            score -= individualScore / 2;
                        }
                        // Set the score and round it
                        results.setScore((int) score);
                        return results;
                    }
                }
            }
        } catch (URISyntaxException ex) {
            // Catch and throw our exception
            logger.error("Failed to format Amazon Completion URL Properly", ex);
            throw ex;
        }

        // If we've reached this point no suggestion was found that matched what we were looking for. This means the score is 0, try again!
        results.setScore(0);
        return results;
    }

    private Status isSuggestionPresentInArray(JSONArray array, String suggestion) {
        // If the array isn't at least 2 long then it's of an incorrect format
        if (array.length() < 2) {
            return Status.NOT_PRESENT;
        }
        // The "Suggestions" returned object has another array in the first index which contains the actual word suggestions
        JSONArray results = array.getJSONArray(1);
        if (results != null) {
            // Go through each provided suggestion
            for (int i = 0; i < results.length(); i++) {
                // If the suggestion is present and it equals our keyword
                if (results.getString(i) != null
                        && results.getString(i).equalsIgnoreCase(suggestion)) {
                    // Check to see if it's the first record or not
                    if (i == 0) {
                        return Status.FIRST;
                    }
                    return Status.PRESENT;
                }
            }
        }

        // Not found
        return Status.NOT_PRESENT;
    }

    enum Status {
        FIRST(true),
        PRESENT(true),
        NOT_PRESENT(false);

        private final boolean isPresent;

        Status(boolean isPresent) {
            this.isPresent = isPresent;
        }

        public boolean isPresent() {
            return isPresent;
        }
    }
}
