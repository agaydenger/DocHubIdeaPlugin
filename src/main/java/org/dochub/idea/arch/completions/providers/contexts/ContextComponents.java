package org.dochub.idea.arch.completions.providers.contexts;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import org.dochub.idea.arch.completions.filters.IDSuggestionFilter;
import org.dochub.idea.arch.completions.filters.KeysAlreadyInListFilter;
import org.dochub.idea.arch.completions.providers.Contexts;
import org.dochub.idea.arch.completions.providers.suggets.IDSuggestComponents;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLSequenceItem;

public class ContextComponents extends IDSuggestComponents {
    private static String keyword = "components";

    @Override
    protected ElementPattern<? extends PsiElement> getPattern() {
        return PlatformPatterns.psiElement()
                .withSuperParent(2, psi(YAMLSequenceItem.class))
                .withSuperParent(4,
                        psi(YAMLKeyValue.class)
                                .withName(PlatformPatterns.string().equalTo(keyword))
                                .and(Contexts.rootPattern)
                );
    }

    @Override
    protected IDSuggestionFilter getFilterForAlreadyExistsKeys() {
        return new KeysAlreadyInListFilter();
    }
}
