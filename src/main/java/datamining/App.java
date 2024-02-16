package datamining;

public class App {
    public static void main(String[] args) {
        try {
            boolean askGPT = false; // change this to switch between ChatGPT enhanced mode or normal
            int nrOfDocs;

            DollarStoreWatson watson = new DollarStoreWatson(
                    "src\\main\\resources\\index",
                    "src\\main\\resources\\questions.txt",
                    askGPT
            );

            if (askGPT) // uses prerecorded answers from ChatGPT, see resources folder
                nrOfDocs = 10;
            else        // use normal document retrieval
                nrOfDocs = 1;

            watson.parseAllQuestions(nrOfDocs); // parse and answer questions
        }

        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}
