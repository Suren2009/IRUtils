# Plural Singular

A small Android app (Java) plus a reusable utility class, `EnglishInflector`, that returns the
singular and plural forms of English words.

The utility is rule based and **does not use a stemmer**, so it only adds or removes plural
endings. Words like "Training" stay as they are instead of becoming "Train".

| Input      | Singular   | Plural      |
|------------|------------|-------------|
| Train      | Train      | Trains      |
| Trains     | Train      | Trains      |
| Training   | Training   | Trainings   |
| Cities     | City       | Cities      |
| Children   | Child      | Children    |
| analysis   | analysis   | analyses    |
| bus        | bus        | buses       |
| sheep      | sheep      | sheep       |

## Utility API

`app/src/main/java/com/example/inflector/util/EnglishInflector.java` is plain Java with no
Android dependencies, so you can copy it into any project.

```java
EnglishInflector.toPlural("Train");        // "Trains"
EnglishInflector.toSingular("Trains");     // "Train"
EnglishInflector.isPlural("Trains");       // true

EnglishInflector.WordForms f = EnglishInflector.getForms("Training");
f.singular;                                // "Training"
f.plural;                                  // "Trainings"

EnglishInflector.getTokens("train stations");
// [train, trains, station, stations]
```

How it works, in order:

1. Uncountable words (sheep, news, series, information, ...) keep the same form.
2. Irregular words (man/men, child/children, knife/knives, cactus/cacti, crisis/crises, ...)
   and irregular compounds (fireman/firemen, grandchild/grandchildren).
3. Exception lists for words that look plural but are singular (bus, gas, lens, analysis,
   class) and for singulars ending in `-ie`, `-che` and `-use` (movies, caches, causes).
4. Suffix rules: `-s/-x/-z/-sh/-ch` take `-es`, consonant + `-y` becomes `-ies`, otherwise `-s`.

The letter case of the input is kept ("TRAIN" becomes "TRAINS").

English has many exceptions, so this covers common nouns. Add new cases to the
lists at the top of the class if you need more.

## App

There is one screen with a text box. As you type a word or phrase, the app shows the singular and
plural form of each word and the combined list of tokens.

## Build and test

Requires JDK 17+ and the Android SDK (platform 34).

```bash
./gradlew assembleDebug        # APK in app/build/outputs/apk/debug/
./gradlew testDebugUnitTest    # unit tests for EnglishInflector
```
