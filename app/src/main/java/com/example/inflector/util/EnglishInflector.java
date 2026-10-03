package com.example.inflector.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rule-based singular/plural converter for English nouns.
 *
 * <p>Unlike a stemmer, this only adds or removes inflectional plural endings, so words such as
 * "Training" are left intact ("Training" -> "Trainings") instead of being reduced to "Train".
 *
 * <p>Conversion order: uncountable words, irregular words (and a few irregular compounds such as
 * "fireman"), exception lists, then suffix rules. The letter case of the input is preserved
 * ("Train" -> "Trains", "TRAIN" -> "TRAINS").
 */
public final class EnglishInflector {

    private static final Pattern WORD = Pattern.compile("[A-Za-z]+");

    /** Words with the same singular and plural form, or with no meaningful plural. */
    private static final Set<String> UNCOUNTABLE = setOf(
            "sheep", "fish", "deer", "moose", "swine", "bison", "salmon", "trout", "shrimp",
            "squid", "cod", "aircraft", "spacecraft", "hovercraft", "offspring", "series",
            "species", "means", "news", "information", "equipment", "rice", "money", "advice",
            "furniture", "luggage", "baggage", "knowledge", "music", "software", "hardware",
            "firmware", "feedback", "homework", "research", "evidence", "traffic", "weather",
            "water", "milk", "bread", "butter", "sugar", "sand", "staff", "police", "cattle",
            "jeans", "trousers", "pants", "shorts", "scissors", "pliers", "tongs",
            "headquarters", "barracks", "crossroads", "gallows", "physics", "mathematics",
            "economics", "ethics", "politics", "athletics", "gymnastics", "linguistics",
            "statistics", "electronics", "logistics", "genetics", "mumps", "measles",
            "diabetes", "rabies", "chess", "billiards", "darts", "data", "media");

    /** Words ending in "s" that are not plurals and must not lose their trailing "s". */
    private static final Set<String> SINGULAR_ENDING_IN_S = setOf(
            "gas", "atlas", "canvas", "alias", "bias", "lens", "iris", "chaos", "cosmos",
            "pancreas", "christmas", "kudos", "ethos", "pathos", "thermos", "plus",
            "minus", "bus", "yes", "has", "was", "does", "is", "its", "his", "hers", "ours",
            "yours", "theirs", "this", "thus", "us", "always", "perhaps", "unless", "towards",
            "afterwards", "besides", "sometimes", "nevertheless", "less", "across");

    /** Irregular singular -> plural pairs. The reverse map is derived from this one. */
    private static final Map<String, String> IRREGULAR_PLURALS = new HashMap<>();
    private static final Map<String, String> IRREGULAR_SINGULARS = new HashMap<>();

    static {
        String[][] pairs = {
                {"man", "men"}, {"woman", "women"}, {"child", "children"}, {"person", "people"},
                {"tooth", "teeth"}, {"foot", "feet"}, {"goose", "geese"}, {"mouse", "mice"},
                {"louse", "lice"}, {"ox", "oxen"}, {"die", "dice"}, {"penny", "pence"},
                // -f / -fe -> -ves
                {"leaf", "leaves"}, {"loaf", "loaves"}, {"thief", "thieves"},
                {"sheaf", "sheaves"}, {"half", "halves"}, {"calf", "calves"},
                {"elf", "elves"}, {"self", "selves"}, {"shelf", "shelves"},
                {"wolf", "wolves"}, {"knife", "knives"}, {"wife", "wives"},
                {"life", "lives"}, {"midwife", "midwives"}, {"housewife", "housewives"},
                {"scarf", "scarves"}, {"wharf", "wharves"}, {"hoof", "hooves"},
                // Latin / Greek
                {"cactus", "cacti"}, {"focus", "foci"}, {"fungus", "fungi"},
                {"nucleus", "nuclei"}, {"radius", "radii"}, {"stimulus", "stimuli"},
                {"syllabus", "syllabi"}, {"alumnus", "alumni"}, {"corpus", "corpora"},
                {"genus", "genera"}, {"phenomenon", "phenomena"}, {"criterion", "criteria"},
                {"datum", "data"}, {"medium", "media"}, {"bacterium", "bacteria"},
                {"curriculum", "curricula"}, {"memorandum", "memoranda"},
                {"stratum", "strata"}, {"addendum", "addenda"}, {"erratum", "errata"},
                {"appendix", "appendices"}, {"index", "indices"}, {"matrix", "matrices"},
                {"vertex", "vertices"}, {"vortex", "vortices"}, {"apex", "apices"},
                {"axis", "axes"}, {"crisis", "crises"}, {"oasis", "oases"},
                {"diagnosis", "diagnoses"}, {"prognosis", "prognoses"},
                {"neurosis", "neuroses"}, {"basis", "bases"}, {"larva", "larvae"},
                {"alga", "algae"}, {"antenna", "antennae"}, {"formula", "formulae"},
                {"vertebra", "vertebrae"}, {"nebula", "nebulae"},
                // -o -> -oes
                {"potato", "potatoes"}, {"tomato", "tomatoes"}, {"hero", "heroes"},
                {"echo", "echoes"}, {"veto", "vetoes"}, {"torpedo", "torpedoes"},
                {"embargo", "embargoes"}, {"domino", "dominoes"}, {"volcano", "volcanoes"},
                {"mosquito", "mosquitoes"}, {"tornado", "tornadoes"}, {"buffalo", "buffaloes"},
                // misc
                {"quiz", "quizzes"}, {"fez", "fezzes"}, {"whiz", "whizzes"},
        };
        for (String[] pair : pairs) {
            IRREGULAR_PLURALS.put(pair[0], pair[1]);
            IRREGULAR_SINGULARS.put(pair[1], pair[0]);
        }
        // "bases" is far more often the plural of "base" than of "basis".
        IRREGULAR_SINGULARS.remove("bases");
    }

    /** Singular nouns ending in "-ie": "movies" -> "movie", not "movy". */
    private static final Set<String> IE_SINGULARS = setOf(
            "movie", "cookie", "pie", "tie", "lie", "zombie", "rookie", "calorie", "prairie",
            "brownie", "hippie", "selfie", "smoothie", "sweetie", "freebie", "goalie", "genie",
            "auntie", "birdie", "boogie", "budgie", "camaraderie", "collie", "coolie",
            "eyrie", "hoodie", "indie", "lingerie", "magpie", "menagerie", "newbie", "oldie",
            "pixie", "reverie", "rotisserie", "talkie", "techie", "toughie", "veggie", "yuppie",
            "bootie", "cutie", "foodie", "groupie", "junkie", "quickie", "roomie", "sortie",
            "specie", "walkie", "aussie", "bogie", "cabbie", "commie", "kiddie");

    /** Singular nouns ending in "-che": "caches" -> "cache", not "cach". */
    private static final Set<String> CHE_SINGULARS = setOf(
            "cache", "niche", "ache", "headache", "toothache", "stomachache", "backache",
            "avalanche", "moustache", "mustache", "psyche", "cliche", "quiche", "creche",
            "douche", "microfiche", "panache", "attache", "brioche", "fiche", "pastiche",
            "tranche", "gouache", "earache", "heartache");

    /** Singular nouns ending in "-use": "causes" -> "cause", not "caus". */
    private static final Set<String> USE_SINGULARS = setOf(
            "use", "cause", "house", "pause", "clause", "blouse", "spouse", "mouse", "excuse",
            "abuse", "fuse", "muse", "refuse", "accuse", "amuse", "confuse", "diffuse",
            "infuse", "misuse", "reuse", "overuse", "disuse", "recluse", "ruse", "peruse",
            "warehouse", "greenhouse", "lighthouse", "farmhouse", "applause", "carouse",
            "douse", "grouse", "rouse", "arouse", "menopause");

    /** "-man" words that are not compounds of "man": "humans", not "humen". */
    private static final Set<String> MAN_NOT_COMPOUND = setOf(
            "human", "german", "roman", "shaman", "caiman", "cayman", "talisman", "ottoman",
            "doberman", "walkman", "desman", "dolman", "hetman", "ataman", "oman", "pullman",
            "superhuman", "inhuman", "subhuman");

    /** Singular nouns ending in "-men" that are not plurals of "-man". */
    private static final Set<String> MEN_SINGULARS = setOf(
            "omen", "amen", "abdomen", "specimen", "regimen", "stamen", "acumen", "hymen",
            "semen", "ramen", "yemen", "lumen", "bitumen", "albumen", "cyclamen", "dolmen",
            "germen", "tegmen", "rumen", "foramen", "noumen");

    /** Compound suffixes that inherit an irregular plural: "grandchild" -> "grandchildren". */
    private static final String[][] IRREGULAR_COMPOUND_SUFFIXES = {
            {"woman", "women"}, {"man", "men"}, {"child", "children"}, {"person", "people"},
    };

    private EnglishInflector() {
    }

    /** Singular and plural form of a single word. */
    public static final class WordForms {
        public final String original;
        public final String singular;
        public final String plural;
        public final boolean inputIsPlural;
        public final boolean uncountable;

        WordForms(String original, String singular, String plural, boolean inputIsPlural,
                  boolean uncountable) {
            this.original = original;
            this.singular = singular;
            this.plural = plural;
            this.inputIsPlural = inputIsPlural;
            this.uncountable = uncountable;
        }

        @Override
        public String toString() {
            return original + " -> singular: " + singular + ", plural: " + plural;
        }
    }

    /** Returns both forms of {@code word}, deciding whether the input is singular or plural. */
    public static WordForms getForms(String word) {
        String w = word == null ? "" : word.trim();
        boolean uncountable = isUncountable(w);
        if (isPlural(w)) {
            return new WordForms(w, toSingular(w), w, true, uncountable);
        }
        return new WordForms(w, w, toPlural(w), false, uncountable);
    }

    /**
     * Splits {@code text} into words and returns the forms of each one, in input order.
     * Duplicate words (case-insensitive) are reported once.
     */
    public static List<WordForms> getFormsForText(String text) {
        List<WordForms> result = new ArrayList<>();
        if (text == null) {
            return result;
        }
        Set<String> seen = new HashSet<>();
        Matcher m = WORD.matcher(text);
        while (m.find()) {
            String token = m.group();
            if (seen.add(token.toLowerCase(Locale.ROOT))) {
                result.add(getForms(token));
            }
        }
        return result;
    }

    /**
     * Returns the distinct lower-case singular and plural tokens for every word in {@code text},
     * e.g. "Trains" -> [train, trains]. Useful for building search queries.
     */
    public static Set<String> getTokens(String text) {
        Set<String> tokens = new LinkedHashSet<>();
        for (WordForms forms : getFormsForText(text)) {
            tokens.add(forms.singular.toLowerCase(Locale.ROOT));
            tokens.add(forms.plural.toLowerCase(Locale.ROOT));
        }
        return Collections.unmodifiableSet(tokens);
    }

    public static boolean isUncountable(String word) {
        return word != null && UNCOUNTABLE.contains(word.toLowerCase(Locale.ROOT));
    }

    /** True if {@code word} looks like a plural noun. Uncountable words count as both. */
    public static boolean isPlural(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }
        String lower = word.toLowerCase(Locale.ROOT);
        if (UNCOUNTABLE.contains(lower)) {
            return true;
        }
        return !singularizeLower(lower).equals(lower);
    }

    public static boolean isSingular(String word) {
        return isUncountable(word) || !isPlural(word);
    }

    /** Converts a singular noun to its plural form. Words already plural are returned as-is. */
    public static String toPlural(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }
        String lower = word.toLowerCase(Locale.ROOT);
        if (UNCOUNTABLE.contains(lower) || isPlural(lower)) {
            return word;
        }
        return matchCase(word, pluralizeLower(lower));
    }

    /** Converts a plural noun to its singular form. Words already singular are returned as-is. */
    public static String toSingular(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }
        return matchCase(word, singularizeLower(word.toLowerCase(Locale.ROOT)));
    }

    private static String pluralizeLower(String w) {
        String irregular = IRREGULAR_PLURALS.get(w);
        if (irregular != null) {
            return irregular;
        }
        String compound = applyCompoundSuffix(w, true);
        if (compound != null) {
            return compound;
        }
        if (w.endsWith("ysis") || w.endsWith("thesis")) {
            return w.substring(0, w.length() - 2) + "es";
        }
        if (w.endsWith("s") || w.endsWith("x") || w.endsWith("z")
                || w.endsWith("sh") || w.endsWith("ch")) {
            return w + "es";
        }
        if (w.endsWith("y") && w.length() > 1
                && (!isVowel(w.charAt(w.length() - 2)) || w.endsWith("quy"))) {
            return w.substring(0, w.length() - 1) + "ies";
        }
        return w + "s";
    }

    private static String singularizeLower(String w) {
        if (w.length() < 3 || UNCOUNTABLE.contains(w) || SINGULAR_ENDING_IN_S.contains(w)) {
            return w;
        }
        String irregular = IRREGULAR_SINGULARS.get(w);
        if (irregular != null) {
            return irregular;
        }
        if (IRREGULAR_PLURALS.containsKey(w)) {
            return w;
        }
        String compound = applyCompoundSuffix(w, false);
        if (compound != null) {
            return compound;
        }
        if (!w.endsWith("s")
                || w.endsWith("ss") || w.endsWith("us") || w.endsWith("is")
                || w.endsWith("ous") || w.endsWith("'s")) {
            return w;
        }

        String stem = w.substring(0, w.length() - 1);   // without "s"
        String stemEs = w.substring(0, w.length() - 2); // without "es"

        if (w.endsWith("ies")) {
            String base = w.substring(0, w.length() - 3);
            if (base.length() <= 1 || IE_SINGULARS.contains(stem)) {
                return stem;
            }
            return base + "y";
        }
        if (w.endsWith("es")) {
            if (SINGULAR_ENDING_IN_S.contains(stemEs)) {
                return stemEs;                                   // gases, lenses
            }
            if (w.endsWith("sses") || w.endsWith("shes") || w.endsWith("xes")
                    || w.endsWith("zzes") || w.endsWith("tzes")) {
                return stemEs;                                   // classes, dishes, boxes
            }
            if (w.endsWith("ches")) {
                return CHE_SINGULARS.contains(stem) ? stem : stemEs; // churches vs caches
            }
            if (w.endsWith("uses")) {
                if (USE_SINGULARS.contains(stem) || endsWithCompound(stem, USE_SINGULARS)) {
                    return stem;                                 // causes, houses
                }
                return stemEs;                                   // buses, statuses
            }
            if (w.endsWith("yses") || w.endsWith("theses")) {
                return stemEs + "is";                            // analyses, hypotheses
            }
        }
        return stem;
    }

    /** Handles irregular compounds such as "fireman", "policewomen", "grandchild". */
    private static String applyCompoundSuffix(String w, boolean toPlural) {
        for (String[] pair : IRREGULAR_COMPOUND_SUFFIXES) {
            String from = toPlural ? pair[0] : pair[1];
            String to = toPlural ? pair[1] : pair[0];
            if (w.length() <= from.length() || !w.endsWith(from)) {
                continue;
            }
            String result = w.substring(0, w.length() - from.length()) + to;
            if (pair[0].equals("man")) {
                String singular = toPlural ? w : result;
                if (MAN_NOT_COMPOUND.contains(singular) || MEN_SINGULARS.contains(w)) {
                    return null;
                }
            }
            return result;
        }
        return null;
    }

    /** Matches compounds like "warehouse"; short entries ("use", "ruse") would over-match. */
    private static boolean endsWithCompound(String w, Set<String> suffixes) {
        for (String suffix : suffixes) {
            if (suffix.length() >= 5 && w.length() > suffix.length() && w.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isVowel(char c) {
        return "aeiou".indexOf(c) >= 0;
    }

    /** Applies the letter case of {@code original} to {@code lower}. */
    private static String matchCase(String original, String lower) {
        if (original.length() > 1 && original.equals(original.toUpperCase(Locale.ROOT))) {
            return lower.toUpperCase(Locale.ROOT);
        }
        if (Character.isUpperCase(original.charAt(0))) {
            return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
        }
        return lower;
    }

    private static Set<String> setOf(String... values) {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(values)));
    }
}
