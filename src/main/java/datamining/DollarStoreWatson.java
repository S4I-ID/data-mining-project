package datamining;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.MultiSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class DollarStoreWatson {
    private boolean askGPT;
    private String questionFile;
    private Analyzer analyzer;
    private IndexSearcher indexSearcher;

    /**
     * Constructor
     *
     * @param indexDir         Path to index
     * @param questionFilePath Path to file containing questions to answer
     */
    public DollarStoreWatson(String indexDir, String questionFilePath, boolean askGPT) throws IOException {
        questionFile = questionFilePath;
        analyzer = new EnglishAnalyzer();
        indexSearcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(Paths.get(indexDir))));
        indexSearcher.setSimilarity(
                new MultiSimilarity(
                        new Similarity[]{
                                new BM25Similarity(1.2f, 0.15f)
                        }
                ));
        this.askGPT = askGPT;
    }

    /**
     * Reads all questions and answers from a file and searches for the document title which matches the answers from
     * a prebuilt index.
     *
     * @throws Exception if file does not exist / is empty or the parser is incorrectly set up
     */
    public void parseAllQuestions(int nrOfDocuments) throws Exception {
        Scanner scanner = new Scanner(new File(questionFile));     // open questions file
        double correct = 0;
        int questionNr = 0;
        ImprovementClass ic = new ImprovementClass();

        while (scanner.hasNextLine()) {                         // while file still has lines to be read
            String categoryLine = scanner.nextLine();               // read question category
            String textLine = scanner.nextLine();                   // read question text
            String answerLine = scanner.nextLine().toLowerCase();   // read question answer
            scanner.nextLine();     // read the empty line between answers to prepare scanner position for next loop

            questionNr++;
            //if (questionNr==25)
                //throw new Exception("stopped");
            System.out.println("\n      --- QUESTION " + questionNr);

            categoryLine = categoryLine.replaceAll("\\(.*\\)", "");
            String categoryWithTextLine = categoryLine + " " + textLine;  // create query text from question and format it
            String inputQueryText = processInputText(categoryWithTextLine);
            Query q = new QueryParser("data", analyzer).parse(QueryParser.escape(inputQueryText));  // create query

            if (!askGPT) {
                TopDocs topDocuments = indexSearcher.search(q, nrOfDocuments);   // get top nrOfDocs documents in index for query
                for (ScoreDoc scoreDoc : topDocuments.scoreDocs) {   // for each document in top # of documents
                    Document answerDoc = indexSearcher.doc(scoreDoc.doc); // get document
                    for (String answer : answerLine.split("\\|")) {
                        answer = answer.trim(); // remove trailing whitespaces
                        System.out.println(inputQueryText);
                        //if (answerDoc.get("title").contains(answer) ) { // if document title contains the answer, mark it as correct
                        if (answerDoc.get("title").equals(answer)) {
                            correct++;  // mark answer as correct
                            System.out.println("    Correct answer  : " + answer + "\n    Returned answer : " + answerDoc.get("title"));
                            break;  // stop checking multiple answers in this case
                        } else {
                            System.out.println("    Correct answer  : " + answer + "\n    INCORRECT answer: " + answerDoc.get("title"));
                        }
                    }
                }
            } else {    // use ChatGPT enhanced document retrieval
                TopDocs topDocuments = indexSearcher.search(q, nrOfDocuments);   // get top nrOfDocs documents in index for query

                System.out.println(textLine);
                List<String> titles = new ArrayList<>();
                int i=0;
                for (ScoreDoc scoreDoc : topDocuments.scoreDocs) { // for each document in top # of documents
                    Document answerDoc = indexSearcher.doc(scoreDoc.doc); // get document
                    titles.add(answerDoc.get("title")); // add all documents to a list
                    System.out.println("Found document: " + i + ". " +  answerDoc.get("title"));
                    i++;
                }
                // get the correct document # from ChatGPT
                int response = ic.preTestedChatGPT(questionNr);
                for (String answer : answerLine.split("\\|")) {
                    answer = answer.trim();
                    if (titles.get(response).contains(answer)) {
                        correct++;
                        System.out.println("    Correct answer  : " + answer + "\n    Returned answer : " + titles.get(response));
                        break;
                    } else {
                        System.out.println("    Correct answer  : " + answer + "\n    INCORRECT answer: " +  titles.get(response));
                    }
                }
            }
        }
        printStatistics(correct, questionNr);
        scanner.close();
    }

    /**
     * Processes query lines, removes special characters and structures
     *
     * @param inputQueryText input line
     * @return processed line
     */
    private String processInputText(String inputQueryText) {
        return inputQueryText
                .toLowerCase().replaceAll("\\.|\\,|\\'s|\\\"|\\(|\\)|,|!|", "")
                .replaceAll(",|\\\"|\\'s|→|;|!|\\?|&|\\n|\\r", "")
                .replaceAll("\\[|\\]|\\(|\\)|\\:|\\.|=|\\*|\\||\\/|—|\\-", " ")
                .replaceAll(" +", " ");
    }

    /**
     * Prints accuracy values based on number of correct answers and total number of questions.
     *
     * @param nrOfCorrectAnswers number of correct answers
     * @param totalNrOfAnswers   number of total questions
     */
    public void printStatistics(Double nrOfCorrectAnswers, int totalNrOfAnswers) {
        Double accuracy = nrOfCorrectAnswers / totalNrOfAnswers * 100;
        System.out.println("\n      Accuracy         : " + String.format("%.0f", accuracy) + "%");
        System.out.println("      Correct answers  : " + String.format("%.0f", nrOfCorrectAnswers));
        System.out.println("      Incorrect answers: " + String.format("%.0f", totalNrOfAnswers - nrOfCorrectAnswers) + '\n');
    }
}
