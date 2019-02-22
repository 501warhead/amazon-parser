package io.github.war501head.amazonparser.controller.ws;

import io.github.war501head.amazonparser.service.AmazonParserService;
import io.github.war501head.amazonparser.transfer.ParserResultsTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * The RestController for the AmazonParser application.
 *
 * @see AmazonParserService for business logic methods
 * @author Sean K.
 */
@RestController
public class AmazonParserRestController {

    private final AmazonParserService parserService;

    @Autowired
    public AmazonParserRestController(AmazonParserService parserService) {
        this.parserService = parserService;
    }

    /**
     * Gets the estimated amount that the keyword is searched on Amazon.
     *
     * @param keyword The keyword to search for on Amazon to measure. E.g. "iphone charger"
     * @return A Transfer Object containing the keyword and a number between 1 and 100 that measures how searched the item is, 100 being the best
     * @throws Exception Any possible exceptions that might come from searching.
     */
    @GetMapping("/estimate")
    @ResponseBody
    public ParserResultsTO getEstimate(@RequestParam String keyword) throws Exception {
        return parserService.getEstimate(keyword);
    }

}
