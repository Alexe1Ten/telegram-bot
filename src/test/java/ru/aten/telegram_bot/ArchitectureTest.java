package ru.aten.telegram_bot;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

public class ArchitectureTest {

    private final JavaClasses importedClasses = new ClassFileImporter().importPackages("ru.aten");


    @Test
    void noCyclicDependencies() {
        ArchRule rule = slices()
                .matching("ru.aten.(*)..")
                .should().beFreeOfCycles();

        rule.check(importedClasses);
    }
}
