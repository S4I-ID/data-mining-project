
package datamining;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Builds a document index.
 */
public class IndexBuilder {
    EnglishAnalyzer analyzer;
    IndexWriterConfig config;
    IndexWriter indexWriter;


    public static void main(String[] args) throws IOException {
        IndexBuilder builder = new IndexBuilder();
        builder.buildIndex("src\\main\\resources\\index", "src\\main\\resources\\wikiDocs");
    }

    /**
     * Builds an index at the specified location using the documents in the provided path.
     * @param indexDir Path to build index at
     * @param docPath Directory path with documents
     */
    public void buildIndex(String indexDir, String docPath) throws IOException {
        analyzer = new EnglishAnalyzer();
        config = new IndexWriterConfig(analyzer);
        config.setOpenMode(OpenMode.CREATE);
        indexWriter = new IndexWriter(FSDirectory.open(Paths.get(indexDir)), config);

        final File folder = new File(docPath);
        for (final File file : Objects.requireNonNull(folder.listFiles())) {  // process each wiki document for index
            processFile(file);
        }
        indexWriter.close();
        System.out.println("--- DONE ---");
    }

    /**
     * Processes a file, splitting it into documents and adds it to the index
     * @param file file to be processed
     * @throws IOException
     */
    private void processFile(File file) throws IOException {
        System.out.println("Processing file [ " + file.getName() + " ] ");
        String currentLine;
        String currentTitle = "";
        StringBuilder data = new StringBuilder();

        FileReader fileReader = new FileReader(file);
        BufferedReader reader = new BufferedReader(fileReader);

        while ((currentLine = reader.readLine()) != null) {
            currentLine = currentLine.trim();
            currentLine = currentLine.toLowerCase();

            if (    Pattern.matches("\\[\\[(.*?)\\]\\]", currentLine) &&
                    !Pattern.matches("(.*)?file:", currentLine
                )) {    // title line
                if (currentTitle.equals("")) { // first title in document
                    currentTitle = currentLine.substring(2, currentLine.length() - 2); // remove title
                }
                else {  // other titles
                    String finalData = data.toString().replaceAll("[[\n\r]]", " ");
                    addDoc(indexWriter, currentTitle, finalData);
                    //System.out.println(" ----- " + currentTitle + "  === " +file.getName());
                    //System.out.println(data);
                    currentTitle = currentLine.substring(2, currentLine.length() - 2);
                    data = new StringBuilder();
                }
            }
            else if (   // lines we do not want
                //Pattern.matches("==(.*?)==", currentLine) ||
                        Pattern.matches("=+references=+|=+see also=+|=+bibliography=+|=+external links=+|=+further reading=+|=+notes=+|=+gallery=+|=+footnotes=+", currentLine) ||
                        Pattern.matches("\\}\\}|\\{\\{|", currentLine) ||
                        Pattern.matches("^\\||^file:|^image:|", currentLine) ||
                        Pattern.matches("\\[\\[file:", currentLine) ||
                        Pattern.matches("\\[\\[image:", currentLine) ||
                        Pattern.matches("^file:", currentLine) ||
                        Pattern.matches("image:(.*?)", currentLine) ||
                        Pattern.matches("\\|(.*?)", currentLine)
            ) {
                continue;
            }
            else if (   // other data lines
                    Pattern.matches("#redirect(.*?)", currentLine) ||
                    currentLine.startsWith("categories:")
                    //Pattern.matches("^==(.*)?==$", currentLine)
                    ) {
                currentLine = processDataLine(currentLine);
                currentLine = currentLine.replaceAll("redirect|image:|\\||file:|categories:", "");
                data.append(currentLine);
            }
            else {      // data lines
                currentLine = processDataLine(currentLine);
                currentLine = currentLine.replaceAll(" a | the | an | and | it | for | or | but | in | my | your | our | their ", " ");
                data.append(currentLine);
            }
        }
        String finalData = data.toString();                 // build data string
        addDoc(indexWriter, currentTitle, finalData);       // add last processed document

    }

    /**
     * Adds document to index.
     * @param indexWriter index writer
     * @param title title of document
     * @param data content of document
     */
    private static void addDoc(IndexWriter indexWriter, String title, String data) throws IOException {
        Document document = new Document();
        document.add(new StringField("title", title, Field.Store.YES));
        document.add(new TextField("data", data, Field.Store.YES));
        indexWriter.addDocument(document);
    }

    /**
     * Processes a String data line, removing special characters and structures from it.
     * @param line input line to be processed
     * @return String - processed line
     */
    private static String processDataLine(String line) {
        return line .replaceAll("http|https|www|", "")          // can't remove links so this should work for now
                    .replaceAll("\\[tpl\\].*\\[/tpl\\]", "")    // replace tpl links
                    .replaceAll("[\\[\\]():=*|/—=}{#_]", " ")   // replace special chars
                    .replaceAll("tpl|,|\\\"|\\-|\\'s|→|;|!|\\?|&| ref |\\.|<br>|''", "")    // replace more special chars
                    .replaceAll(" +|\n|\r", " ")    // remove newlines and CRs
                    .replaceAll("isbn [0-9]+", ""); // remove isbn codes
    }

}
