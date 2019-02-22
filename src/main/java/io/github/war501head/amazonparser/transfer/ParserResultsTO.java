package io.github.war501head.amazonparser.transfer;

/**
 * Represents a returned JSON payload, including the keyword and the score we've assigned to it based on it's search visibility
 *
 * @author Sean K
 */
public class ParserResultsTO {
    private String keyword;
    private int score;

    public ParserResultsTO() {
    }

    /**
     * @return The keyword being searched
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * Sets the Keyword being searched
     *
     * @param keyword Keyword being searched
     */
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    /**
     * @return The score of this keyword
     */
    public int getScore() {
        return score;
    }

    /**
     * Set the score of this payload
     *
     * @param score Score of this payload associated with the keyword
     */
    public void setScore(int score) {
        this.score = score;
    }
}
