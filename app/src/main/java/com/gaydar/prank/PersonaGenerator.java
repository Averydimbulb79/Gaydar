package com.gaydar.prank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Random comedy copy only. No photo data enters this class. */
public final class PersonaGenerator {
    private static final String[] DESCRIPTORS = {
        "Cosmic", "Flaming", "Macho", "Disco", "Velvet", "Neon", "Glitter", "Royal",
        "Turbo", "Drama", "Legendary", "Electric", "Midnight", "Diamond", "Chaotic",
        "Fabulous", "Galactic", "Platinum", "Spicy", "Golden", "Sassy", "Lunar",
        "Iconic", "Supersonic", "Majestic", "Champagne", "Wild", "Radiant", "Dazzling", "Unhinged"
    };
    private static final String[] ROLES = {"Top", "Bottom", "Flex", "Side"};
    private static final String[] WILDCARDS = {
        "Leather Legend", "Uniform Enthusiast", "Latex Luminary", "Boot Admirer",
        "Costume Connoisseur", "Silk Superstar", "Masked Mystery", "Roleplay Royalty",
        "Velvet Visionary", "Harness Hero", "Glove Devotee", "Satin Sensation"
    };
    private static final String[] TAGLINES = {
        "Main-character energy. Zero scientific basis.", "Too fabulous for the algorithm.",
        "Serving chaos with excellent posture.", "A walking plot twist in premium packaging.",
        "Certified by absolutely nobody.", "The stars made this up. So did we.",
        "Arrived late. Stole the entire scene.", "Confidence: astronomical. Evidence: imaginary."
    };
    public static final List<String> PERSONAS;
    static {
        List<String> names = new ArrayList<>();
        for (String descriptor : DESCRIPTORS) for (String role : ROLES) names.add(descriptor + " " + role);
        PERSONAS = Collections.unmodifiableList(names);
    }
    private final Random random;
    private int last = -1;
    public PersonaGenerator() { this(new Random()); }
    PersonaGenerator(Random random) { this.random = random; }
    public Result next() {
        int index;
        do { index = random.nextInt(PERSONAS.size()); } while (index == last);
        last = index;
        return new Result(PERSONAS.get(index), 70 + random.nextInt(31),
            WILDCARDS[random.nextInt(WILDCARDS.length)], TAGLINES[random.nextInt(TAGLINES.length)]);
    }
    public static final class Result {
        public final String persona, wildcard, tagline;
        public final int energy;
        Result(String persona, int energy, String wildcard, String tagline) {
            this.persona = persona; this.energy = energy; this.wildcard = wildcard; this.tagline = tagline;
        }
    }
}
