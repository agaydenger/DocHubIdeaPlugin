package org.dochub.idea.arch.completions.providers;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.ElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.dochub.idea.arch.completions.CompletionKey;
import org.dochub.idea.arch.completions.FormattingInsertHandler;
import org.dochub.idea.arch.utils.PsiUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class FilteredCustomProvider extends CustomProvider {

    @NotNull
    protected abstract ElementPattern<? extends PsiElement> getRootPattern();

    @NotNull
    protected abstract Collection<CompletionKey> getKeys();

    protected abstract int getKeyDocumentLevel();

    @Override
    public @NotNull void appendToCompletion(CompletionContributor completion) {
        completion.extend(
                CompletionType.BASIC,
                getRootPattern(),
                new CompletionProvider<>() {
                    public void addCompletions(@NotNull CompletionParameters parameters,
                                               @NotNull ProcessingContext context,
                                               @NotNull CompletionResultSet resultSet) {

                        YAMLKeyValue parent = PsiTreeUtil.getParentOfType(parameters.getPosition(), YAMLKeyValue.class);
                        PsiElement containerToScan = null;
                        if(parent == null) {
                            // Это корень файла
                            containerToScan = parameters.getPosition().getParent().getContext();
                        } else {
                            containerToScan = PsiTreeUtil.getChildOfType(parent, YAMLMapping.class);
                        }
                        // Тут возможны 2 варианта, либо наш родитель содержит уже филды, либо это просто имя компонента
                        // Если containerToScan != null значит есть хоть 1 поле
                        Set<String> alreadyDefinedAttributes = Optional.ofNullable(containerToScan)
                                .map(c -> PsiUtils.getChildrenOfClass(c, YAMLKeyValue.class)
                                        .map(YAMLKeyValue::getKeyText).collect(Collectors.toSet()))
                                .orElse(Collections.emptySet());

                        getKeys().stream().filter(k -> !alreadyDefinedAttributes.contains(k.getKey()))
                                .map(key -> createLookupElement(key))
                                .forEach(resultSet::addElement);
                    }
                }
        );
    }

    private LookupElementBuilder createLookupElement(CompletionKey key) {
        return LookupElementBuilder.create(key.getKey())
                .withInsertHandler(new FormattingInsertHandler(key, getKeyDocumentLevel()));
    }

}
