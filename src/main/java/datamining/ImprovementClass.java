package datamining;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImprovementClass {
    private List<Integer> answers;

    public ImprovementClass() { // read prerecorded answers from chatgpt
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File("src\\main\\resources\\chatgptanswers.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        answers = new ArrayList<>();
        while (scanner.hasNextLine()) {
            int answer = Integer.parseInt(scanner.nextLine());
            // System.out.println(answer);
            answers.add(answer);
        }
        scanner.close();
    }

    // Returns the prerecorded answer from ChatGPT for a specific question.
    public int preTestedChatGPT(int questionNr) {
        return answers.get(questionNr-1);
    }
    // Connects to ChatGPT API and asks query. Does not work because our payment plan is limited to 3 queries in a minute.
    /*public static int chatGPT(String questionText, List<String> strings) {
        String url = "https://api.openai.com/v1/chat/completions";
        String apiKey = "";
        String model = "gpt-3.5-turbo";


        try {
            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Content-Type", "application/json");

            //String content = "For this question: '" + questionText + "' give the number and ONLY the number in front of of the document that most likely has the correct answer to the question: " +
            //" 0. " + strings.get(0) + " 1. " + strings.get(1) + " 2. " + strings.get(2) + " 3. " + strings.get(3) + " 4. " + strings.get(4);

            String content = "For these documents: " + " 0. " + strings.get(0) + " 1. " + strings.get(1) + " 2. " + strings.get(2) + " 3. " + strings.get(3) + " 4. " + strings.get(4)
                    + " find the one that is most likely the answer to the question: " + questionText + ".Answer with only one number between 0 and 4.";
            String body = "{\"model\": \"" + model + "\", \"temperature\": 0.7 , \"messages\": [{\"role\": \"user\", \"content\": \"" + content + "\"}]}";

            connection.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(body);
            writer.flush();
            writer.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;

            StringBuffer response = new StringBuffer();

            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();

            String res = extractMessageFromJSONResponse(response.toString());
            Matcher matcher = Pattern.compile("[0-4]").matcher(res); // find the first number that the GPT response contains
            matcher.find();
            int i = Integer.parseInt(matcher.group());
            System.out.println("Res: " + i);
            return i;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        /*StringBuilder builder = new StringBuilder();
        builder.append("For these documents: \n ");
        for (int i=0; i<strings.size(); i++) {
            builder.append(i).append(". ").append(strings.get(i)).append("\n ");
        }
        builder.append(" find the one that is most likely the answer to the question: ").append(questionText).append(".Answer with only one number between 0 and 9.");
        System.out.println(builder);
        return 0;
    }

    public static String extractMessageFromJSONResponse(String response) {
        int start = response.indexOf("content") + 11;
        int end = response.indexOf("\"", start);
        return response.substring(start, end);

    }*/

}
