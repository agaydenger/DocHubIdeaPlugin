package org.dochub.idea.arch.completions.providers.components;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import org.dochub.idea.arch.completions.filters.IDSuggestionFilter;
import org.dochub.idea.arch.completions.filters.KeysAlreadyInListFilter;
import org.dochub.idea.arch.completions.providers.Components;
import org.dochub.idea.arch.completions.providers.suggets.IDSuggestAspects;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLSequenceItem;

public class ComponentAspects extends IDSuggestAspects {
    private static String keyword = "aspects";

    @Override
    protected IDSuggestionFilter getFilterForAlreadyExistsKeys() {
        return new KeysAlreadyInListFilter();
    }

    @Override
    protected ElementPattern<? extends PsiElement> getPattern() {
        return PlatformPatterns.psiElement()
                .withSuperParent(2, psi(YAMLSequenceItem.class))
                .withSuperParent(4,
                        psi(YAMLKeyValue.class)
                                .withName(PlatformPatterns.string().equalTo(keyword))
                                .and(Components.rootPattern)
                );
    }
}
