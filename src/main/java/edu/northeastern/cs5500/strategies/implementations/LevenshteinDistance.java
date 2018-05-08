package edu.northeastern.cs5500.strategies.implementations;

import edu.northeastern.cs5500.parsers.PythonToStringParser;
import edu.northeastern.cs5500.strategies.SimilarityStrategy;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class LevenshteinDistance implements SimilarityStrategy {

    @Override
    public Integer[][] getSimilarLineNos(String file1, String file2) {
        String ext1 = FilenameUtils.getExtension(file1);
        String ext2 = FilenameUtils.getExtension(file2);
        /**
         * If single file comparison between two .py files is expected
         */
        if(ext1.equals("py") && ext2.equals("py")){
            String[] fileLines1 = pythonToStringParser.readFile(file1).split("\\r?\\n");
            String[] fileLines2 = pythonToStringParser.readFile(file2).split("\\r?\\n");
            return calculateLineNumbers(fileLines1, fileLines2);
        }
        /**
         * If multiple file comparisons across two .zip files is expected
         */
        else if(ext1.equals("zip") && ext2.equals("zip")){
            return new Integer[0][0];
        }
        else
            throw new IllegalArgumentException();
    }

    private Integer[][] calculateLineNumbers(String[] s1, String[] s2) {
        Map<Integer, Integer> selectedLinesMap;
        Integer[][] similarLineNos;
        selectedLinesMap = new HashMap<>();
        int k=0;
        similarLineNos = new Integer[2][longerLength(s1,s2)];
        for(int i=0; i<2; i++) {
            for (int j = 0; j < similarLineNos[i].length; j++) {
                similarLineNos[i][j] = -1;
            }
        }
        for(String i : s1)
        {
            double max = 0;
            int l=0;
            i = i.trim();
            i = i.replaceAll("\\s+","");
            for (String j : s2)
            {
                j = j.trim();
                j = j.replaceAll("\\s+","");
                double result = (1-(double)getDistance(i.toLowerCase(), j.toLowerCase()) / longer(i, j).length());
                if(!selectedLinesMap.containsKey(l) &&  result > max && result >= 0.85)
                {
                    similarLineNos[1][k] = l;
                    max = result;
                }
                selectedLinesMap.put(similarLineNos[1][k], 1);
                l++;
            }
            k++;
        }

        selectedLinesMap = new HashMap<>();
        k=0;
        for(String i : s2)
        {
            double max = 0;
            int l=0;
            i = i.trim();
            i = i.replaceAll("\\s+","");
            for (String j : s1)
            {
                j = j.trim();
                j = j.replaceAll("\\s+","");
                double result = (1-(double)getDistance(i.toLowerCase(), j.toLowerCase()) / longer(i, j).length());
                if(!selectedLinesMap.containsKey(l) &&  result > max && result >= 0.85)
                {
                    similarLineNos[0][k] = l;
                    max = result;
                }
                selectedLinesMap.put(similarLineNos[0][k], 1);
                l++;
            }
            k++;
        }
        return similarLineNos;

    }

    private final PythonToStringParser pythonToStringParser;

    /**
     * Method to instantiate LevenshteinDistance
     * @param pythonToStringParser PythonToStringParser
     */
    @Autowired
    public LevenshteinDistance(PythonToStringParser pythonToStringParser) {
        this.pythonToStringParser = pythonToStringParser;
    }

    /**
     * Method to calculate edit distance between strings compOne and compTwo using Levenshtein distance
     * @param compOne String
     * @param compTwo String
     * @return edit distance between strings compOne and compTwo using Levenshtein distance double
     */
    public int getDistance(String compOne, String compTwo) {
        int[][] matrix = new int[compOne.length() + 1][compTwo.length() + 1];
        for (int i = 0; i <= compOne.length(); i++) {
            for (int j = 0; j <= compTwo.length(); j++) {
                if (i == 0) {
                    matrix[i][j] = j;
                }
                else if (j == 0) {
                    matrix[i][j] = i;
                }
                else {
                    matrix[i][j] = min(matrix[i - 1][j - 1]
                                    + costOfSubstitution(compOne.charAt(i - 1),
                            compTwo.charAt(j - 1)),
                    matrix[i - 1][j] + 1,
                    matrix[i][j - 1] + 1);
                }
            }
        }
     
        return matrix[compOne.length()][compTwo.length()];
    }
 
    /**
     * Method to determine cost of substitution between two characters
     * @param first char
     * @param second char
     * @return cost of substitution between chars first and second int
     */
    private int costOfSubstitution(char first, char second) {
        return first == second ? 0 : 1;
    }
 
    /**
     * Method to find number with minimum value
     * @param numbers int
     * @return number with minimum value if exists, max integer otherwise int
     */
    private int min(int... numbers) {
        return Arrays.stream(numbers).min().orElse(Integer.MAX_VALUE);
    }
    
    /**
     * Method to return longer string
     * @param s1 String
     * @param s2 String
     * @return longer of the two strings s1 and s2 String
     */
    public String longer(String s1, String s2) {
    	return s1.length() >= s2.length() ? s1 : s2;
    }

    /**
     * Method to return length of longer string array
     * @param s1 String array
     * @param s2 String array
     * @return length of the longer string array among s1 and s2 String arrays
     */
    private int longerLength(String[] s1, String[] s2){
        return s1.length >= s2.length ? s1.length : s2.length;
    }

    /* (non-Javadoc)
     * @see edu.northeastern.cs5500.strategies.SimilarityStrategy#calculateSimilarity(java.lang.String, java.lang.String)
     * calculate similarity measure between file1 and file2 using Levenshtein distance strategy (can be .py or .zip files)
     * @param file1 String
     * @param file2 String
     * @return similarity measure between file1 and file2 using Levenshtein distance strategy double
     */
    @Override
    public double calculateSimilarity(String file1, String file2){
        String ext1 = FilenameUtils.getExtension(file1);
        String ext2 = FilenameUtils.getExtension(file2);
        /**
         * If single file comparison between two .py files is expected 
         */
        if(ext1.equals("py") && ext2.equals("py")){
            String fileContentFile1 = pythonToStringParser.readFile(file1).trim();
            String fileContentFile2 = pythonToStringParser.readFile(file2).trim();
            int distance = getDistance(fileContentFile1, fileContentFile2);
            return (1-(double)distance / longer(fileContentFile1, fileContentFile2).length()) * 100;
        }
        /**
         * If multiple file comparisons across two .zip files is expected 
         */
        else if(ext1.equals("zip") && ext2.equals("zip")){
            List<StringBuilder> firstSubmissionFiles = pythonToStringParser.parseFiles(file1);
            List<StringBuilder> secondSubmissionFiles = pythonToStringParser.parseFiles(file2);
            double overallSimilaritySum = 0;
            for (StringBuilder s1 : firstSubmissionFiles) {
                for (StringBuilder s2 : secondSubmissionFiles) {
                    int distance = getDistance(s1.toString(), s2.toString());
                    overallSimilaritySum += (1-(double)distance / longer(s1.toString(), s2.toString()).length()) * 100;
                }
            }
            return overallSimilaritySum/(firstSubmissionFiles.size()*secondSubmissionFiles.size());
        }
        else
        	throw new IllegalArgumentException();
    }

}
