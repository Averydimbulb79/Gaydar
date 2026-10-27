package com.gaydar.prank;
import org.junit.Test;
import java.util.HashSet;
import java.util.Random;
import static org.junit.Assert.*;
public class PersonaGeneratorTest {
    @Test public void catalogHas120UniquePersonasAcrossFourRoles() {
        assertEquals(120, new HashSet<>(PersonaGenerator.PERSONAS).size());
        for (String role : new String[]{"Top", "Bottom", "Flex", "Side"})
            assertEquals(30, PersonaGenerator.PERSONAS.stream().filter(s -> s.endsWith(" " + role)).count());
    }
    @Test public void rerollsStayInRangeAndAvoidImmediateRepeat() {
        PersonaGenerator generator = new PersonaGenerator(new Random(42));
        String previous = "";
        for (int i = 0; i < 2000; i++) {
            PersonaGenerator.Result result = generator.next();
            assertNotEquals(previous, result.persona);
            assertTrue(result.energy >= 70 && result.energy <= 100);
            assertTrue(PersonaGenerator.PERSONAS.contains(result.persona));
            assertFalse(result.wildcard.isEmpty());
            previous = result.persona;
        }
    }
}
