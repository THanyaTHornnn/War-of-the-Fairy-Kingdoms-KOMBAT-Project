package strategy.parser;

public class Main {
    public static void main(String[] args) {
        String s = "if (hp > 3) then move up else done";

        Tokenizer tokenizer = new Tokenizer(s);
        tokenizer.tokenize().forEach(System.out::println);
    }
}
