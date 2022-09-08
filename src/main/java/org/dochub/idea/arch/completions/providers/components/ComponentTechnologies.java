package org.dochub.idea.arch.completions.providers.components;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import org.dochub.idea.arch.completions.filters.IDSuggestionFilter;
import org.dochub.idea.arch.completions.filters.KeysAlreadyInListFilter;
import org.dochub.idea.arch.completions.providers.Components;
import org.dochub.idea.arch.completions.providers.suggets.IDSuggest;
import org.dochub.idea.arch.utils.PsiUtils;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLSequenceItem;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ComponentTechnologies extends IDSuggest {

    private static final String ELEMENT_WITH_IDS_IN_ROOT_DEFINITION = "items";

    @Override
    protected ElementPattern<? extends PsiElement> getPattern() {
        return PlatformPatterns.psiElement()
                .withSuperParent(2, psi(YAMLSequenceItem.class))
                .withSuperParent(4,
                        psi(YAMLKeyValue.class)
                                .withName(PlatformPatterns.string().equalTo(getSection()))
                                .and(Components.rootPattern)
                );
    }

    @Override
    protected String getSection() {
        return "technologies";
    }

    @Override
    protected IDSuggestionFilter getFilterForAlreadyExistsKeys() {
        return new KeysAlreadyInListFilter();
    }

    @Override
    protected Function<PsiElement, Set<String>> getIdsExtractor() {
        return psiElement -> PsiUtils.getChildrenOfClass(psiElement, YAMLKeyValue.class)
                    .filter(kv -> ELEMENT_WITH_IDS_IN_ROOT_DEFINITION.equals(kv.getKeyText()))
                    .flatMap(kv -> PsiUtils.getChildrenOfClass(kv.getValue(), YAMLKeyValue.class))
                    .map(YAMLKeyValue::getKeyText)
                    .collect(Collectors.toSet());

    }
}
