package com.example.inflector.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.Test;

public class EnglishInflectorTest {

    private static void assertPair(String singular, String plural) {
        assertEquals("plural of " + singular, plural, EnglishInflector.toPlural(singular));
        assertEquals("singular of " + plural, singular, EnglishInflector.toSingular(plural));
    }

    @Test
    public void examplesFromRequirement() {
        assertEquals("Trains", EnglishInflector.toPlural("Train"));
        assertEquals("Train", EnglishInflector.toSingular("Trains"));

        EnglishInflector.WordForms training = EnglishInflector.getForms("Training");
        assertEquals("Training", training.singular);
        assertEquals("Trainings", training.plural);
        assertFalse(training.inputIsPlural);
    }

    @Test
    public void regularRules() {
        assertPair("car", "cars");
        assertPair("box", "boxes");
        assertPair("church", "churches");
        assertPair("dish", "dishes");
        assertPair("class", "classes");
        assertPair("buzz", "buzzes");
        assertPair("waltz", "waltzes");
        assertPair("city", "cities");
        assertPair("day", "days");
        assertPair("key", "keys");
        assertPair("soliloquy", "soliloquies");
        assertPair("photo", "photos");
        assertPair("shoe", "shoes");
        assertPair("glove", "gloves");
        assertPair("roof", "roofs");
        assertPair("size", "sizes");
        assertPair("case", "cases");
    }

    @Test
    public void ieAndCheAndUseWords() {
        assertPair("movie", "movies");
        assertPair("cookie", "cookies");
        assertPair("tie", "ties");
        assertPair("cache", "caches");
        assertPair("headache", "headaches");
        assertPair("cause", "causes");
        assertPair("house", "houses");
        assertPair("warehouse", "warehouses");
        assertPair("bus", "buses");
        assertPair("virus", "viruses");
        assertPair("status", "statuses");
    }

    @Test
    public void irregulars() {
        assertPair("man", "men");
        assertPair("woman", "women");
        assertPair("child", "children");
        assertPair("person", "people");
        assertPair("tooth", "teeth");
        assertPair("mouse", "mice");
        assertPair("knife", "knives");
        assertPair("leaf", "leaves");
        assertPair("wolf", "wolves");
        assertPair("potato", "potatoes");
        assertPair("cactus", "cacti");
        assertPair("analysis", "analyses");
        assertPair("hypothesis", "hypotheses");
        assertPair("crisis", "crises");
        assertPair("criterion", "criteria");
        assertPair("matrix", "matrices");
        assertPair("quiz", "quizzes");
        assertPair("base", "bases");
        assertPair("cheese", "cheeses");
    }

    @Test
    public void compounds() {
        assertPair("fireman", "firemen");
        assertPair("policewoman", "policewomen");
        assertPair("grandchild", "grandchildren");
        assertPair("salesperson", "salespeople");
        assertPair("human", "humans");
        assertPair("german", "germans");
        assertEquals("specimen", EnglishInflector.toSingular("specimen"));
        assertEquals("specimens", EnglishInflector.toPlural("specimen"));
    }

    @Test
    public void singularWordsEndingInSAreKept() {
        for (String w : Arrays.asList("bus", "class", "analysis", "status", "gas", "lens",
                "famous", "this", "process")) {
            assertEquals(w, EnglishInflector.toSingular(w));
            assertFalse(w, EnglishInflector.isPlural(w));
        }
        assertPair("gas", "gases");
        assertPair("lens", "lenses");
    }

    @Test
    public void uncountables() {
        for (String w : Arrays.asList("sheep", "fish", "series", "species", "news",
                "information", "equipment")) {
            assertEquals(w, EnglishInflector.toPlural(w));
            assertEquals(w, EnglishInflector.toSingular(w));
            assertTrue(EnglishInflector.isUncountable(w));
        }
    }

    @Test
    public void noStemming() {
        assertEquals("training", EnglishInflector.toSingular("training"));
        assertEquals("running", EnglishInflector.toSingular("running"));
        assertEquals("happiness", EnglishInflector.toSingular("happiness"));
        assertEquals("trainings", EnglishInflector.toPlural("training"));
    }

    @Test
    public void alreadyPluralIsNotPluralizedAgain() {
        assertEquals("trains", EnglishInflector.toPlural("trains"));
        assertEquals("children", EnglishInflector.toPlural("children"));
        assertEquals("train", EnglishInflector.toSingular("train"));
    }

    @Test
    public void casePreserved() {
        assertEquals("TRAINS", EnglishInflector.toPlural("TRAIN"));
        assertEquals("Children", EnglishInflector.toPlural("Child"));
        assertEquals("City", EnglishInflector.toSingular("Cities"));
    }

    @Test
    public void textTokens() {
        assertEquals(new LinkedHashSet<>(Arrays.asList("train", "trains")),
                EnglishInflector.getTokens("Trains"));
        assertEquals(new LinkedHashSet<>(Arrays.asList("train", "trains", "station", "stations")),
                EnglishInflector.getTokens("train stations"));

        List<EnglishInflector.WordForms> forms =
                EnglishInflector.getFormsForText("Boxes, boxes and CHILDREN!");
        assertEquals(3, forms.size());
        assertEquals("Box", forms.get(0).singular);
        assertEquals("CHILD", forms.get(2).singular);
        assertTrue(EnglishInflector.getTokens("  ").isEmpty());
        assertTrue(EnglishInflector.getTokens(null).isEmpty());
    }
}
