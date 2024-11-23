public class Utils {

    /**
     * Removes all white space.
     */
    public static String strip(String input) {
        if (input == null)
            return null;
        return input.replaceAll("\\s+", "");
    }

}
