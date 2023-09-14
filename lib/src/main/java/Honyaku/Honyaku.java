package Honyaku;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import com.deepl.api.DeepLException;
import com.deepl.api.TextResult;
import com.deepl.api.Translator;

public class Honyaku {

    private static PrintWriter createNewWriter(String filename) {
        PrintWriter output;
        try {
            output = new PrintWriter(
                    new BufferedWriter(new FileWriter(filename)));
        } catch (IOException e) {
            System.err.println("Error finding file to copy to");
            return null;
        }
        return output;
    }

    private static BufferedReader createNewReader(String filename) {
        BufferedReader input;
        try {
            input = new BufferedReader(new FileReader(filename));
        } catch (IOException e) {
            System.err.println("Error opening file");
            return null;
        }
        return input;
    }

    /**
     * Changes the language of the given Anki deck by translating the terms and
     * rewriting the .tsv file holding the cards.
     *
     * @param input
     *            The stream from which .tsv lines will be read from.
     * @param output
     *            The stream to which .tsv formatted lines will be printed to
     *            after being translated.
     * @param translator
     *            The main engine for the translating, powered by DeepL's API.
     * @param termFieldNum
     *            An int storing which column the term to be translated is held.
     * @param throwawayFieldNum
     *            An int storing which column the term to be replaced is held.
     * @param originalLang
     *            The language the front of the card will be in.
     * @param targetLang
     *            The language the back of the card wil be in.
     *
     * @requires termFieldNum < throwawayFieldNum
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws DeepLException
     *
     */
    private static void convertDeck(BufferedReader input, PrintWriter output,
            Translator translator, int termFieldNum, int throwawayFieldNum,
            String originalLang, String targetLang)
            throws IOException, DeepLException, InterruptedException {

        StringTokenizer tokenizer;
        String line;

        while ((line = input.readLine()) != null) {

            tokenizer = new StringTokenizer(line, "\t");
            String targetLangToken = "";

            String csvLine = "";
            String token;
            int i = 1;

            while (tokenizer.hasMoreTokens()) {

                token = tokenizer.nextToken();

                if (i == termFieldNum) {
                    TextResult t = translator.translateText(token, originalLang,
                            targetLang);
                    targetLangToken = t.getText();
                    csvLine += "" + token + "\t";
                } else if (i == throwawayFieldNum) {
                    csvLine += "" + targetLangToken + "\t";
                } else {
                    csvLine += "" + token + "\t";
                }

                i++;
            }

            if (csvLine.endsWith(",")) {
                csvLine = csvLine.substring(0, csvLine.length() - 1);
            }

            output.println(csvLine);

        }

    }

    public static void main(String[] args) throws IOException {

        BufferedReader input = createNewReader("bin/input.txt");
        PrintWriter output = createNewWriter("bin/output.txt");

        String key = "DeepL API key here";
        /*
         * These parameters can be changed depending on the deck being
         * translated. In this case, we want the deck to go from KO-EN to KO-JA,
         * the word we want to translate is in the 1st, column of the
         * spreadsheet, and the English meaning is in the 3rd column.
         */
        int termFieldNum = 1;
        int throwawayFieldNum = 3;
        String originalLang = "KO";
        String targetLang = "JA";

        Translator translator = new Translator(key);

        try {
            convertDeck(input, output, translator, termFieldNum,
                    throwawayFieldNum, originalLang, targetLang);
        } catch (IOException | DeepLException | InterruptedException e) {
            e.printStackTrace();
        }

        output.close();
        input.close();

    }

}
