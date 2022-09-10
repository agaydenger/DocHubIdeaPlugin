package org.dochub.idea.arch.annotators;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import org.dochub.idea.arch.indexing.CacheBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLSequenceItem;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Подсветка ссылок на несуществующие объекты
 */
public class ReferencedObjectUndefinedAnnotator implements Annotator {

    private static final Collection<Analyzer> ANALYZERS = List.of(
            //Ссылка на аспект в компоненте
            new Analyzer("Aspect not found", PlatformPatterns.psiElement()
                    .withSuperParent(1, PlatformPatterns.psiElement(YAMLSequenceItem.class))
                    .withSuperParent(3, PlatformPatterns.psiElement(YAMLKeyValue.class)
                            .withName(PlatformPatterns.string().equalTo("aspects")))
                    .withSuperParent(7, PlatformPatterns.psiElement(YAMLKeyValue.class)
                            .withName(PlatformPatterns.string().equalTo("components"))), "aspects"),
            //Ссылка на компонент в контексте
            new Analyzer("Component not found", PlatformPatterns.psiElement()
                    .withSuperParent(1, PlatformPatterns.psiElement(YAMLSequenceItem.class))
                    .withSuperParent(3, PlatformPatterns.psiElement(YAMLKeyValue.class)
                            .withName(PlatformPatterns.string().equalTo("components")))
                    .withSuperParent(7, PlatformPatterns.psiElement(YAMLKeyValue.class)
                            .withName(PlatformPatterns.string().equalTo("contexts"))), "components"),
            //Ссылка на аспект или компонент в документе
            new Analyzer("Subject not found", PlatformPatterns.psiElement()
                    .withSuperParent(1, PlatformPatterns.psiElement(YAMLSequenceItem.class))
                    .withSuperParent(3, PlatformPatterns.psiElement(YAMLKeyValue.class)
                            .withName(PlatformPatterns.string().equalTo("subjects")))
                    .withSuperParent(7, PlatformPatterns.psiElement(YAMLKeyValue.class)
                            .withName(PlatformPatterns.string().equalTo("docs"))), "aspects", "components")
    );


    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        // Даже не будем проверять остальные элементы
        if (element instanceof YAMLPlainTextImpl) {
            final String elementText = element.getText();
            // Могут быть подстановки с '*' такие будем игнорировать
            if (!elementText.contains("*")) {
                Map<String, CacheBuilder.SectionData> projectCache = CacheBuilder.getProjectCache(element.getProject());

                ANALYZERS.stream().filter(an -> an.getLookupPattern().accepts(element))
                        .filter(an -> an.getCacheNames().stream()
                                .allMatch(cName -> Optional.ofNullable(projectCache.get(cName)).map(c -> c.ids.get(elementText)).isEmpty()))
                        .findAny().ifPresent(an -> holder.newAnnotation(HighlightSeverity.WARNING, an.getMessage()).create());
            }
        }
    }

    private static class Analyzer {
        private final Collection<String> cacheNames;
        private final ElementPattern<? extends PsiElement> lookupPattern;
        private final String message;

        public Analyzer(String message, ElementPattern<? extends PsiElement> lookupPattern, String... cacheNames) {
            this.cacheNames = Set.of(cacheNames);
            this.lookupPattern = lookupPattern;
            this.message = message;
        }

        public Collection<String> getCacheNames() {
            return cacheNames;
        }

        public ElementPattern<? extends PsiElement> getLookupPattern() {
            return lookupPattern;
        }

        public String getMessage() {
            return message;
        }
    }
}
