package datamining;

public class Run {
    public static void main(String[] args) {
        try {
            DollarStoreWatson watson = new DollarStoreWatson(
                    "src\\main\\resources\\index",
                    "src\\main\\resources\\questions.txt"
            );
            watson.parseAllQuestions(1);
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}
