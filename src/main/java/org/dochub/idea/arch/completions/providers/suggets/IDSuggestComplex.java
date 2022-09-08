package org.dochub.idea.arch.completions.providers.suggets;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Key;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.dochub.idea.arch.indexing.CacheBuilder;
import org.dochub.idea.arch.utils.PsiUtils;
import org.dochub.idea.arch.utils.SuggestUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IDSuggestComplex extends BaseSuggest {
    protected ElementPattern<? extends PsiElement> getPattern() {
        return PlatformPatterns.psiElement();
    }

    protected String[] getSections() {
        return new String[]{};
    }
    private Key cacheSectionKey = Key.create("$complex-ids");


    protected Function<PsiElement, Set<String>> getIdsExtractor() {
        return psiElement ->
                PsiUtils.getChildrenOfClass(psiElement, YAMLKeyValue.class)
                        .map(YAMLKeyValue::getKeyText)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
    }

    @Override
    public void appendToCompletion(CompletionContributor completion) {
        completion.extend(
                CompletionType.BASIC,
                getPattern(),
                new CompletionProvider<>() {
                    public void addCompletions(@NotNull CompletionParameters parameters,
                                               @NotNull ProcessingContext context,
                                               @NotNull CompletionResultSet resultSet) {
                        PsiElement psiPosition = parameters.getPosition();
                        Project project = parameters.getPosition().getProject();
                        PsiElement document = PsiUtils.getYamlDocumentByPsiElement(psiPosition);

                        CachedValuesManager cacheManager = CachedValuesManager.getManager(project);

                        Set<String> ids =  cacheManager.getCachedValue(
                                parameters.getOriginalFile(),
                                cacheSectionKey,
                                () -> {
                                    Set<String> suggest = new HashSet<>();
                                    Map<String, CacheBuilder.SectionData> globalCache = getProjectCache(project);
                                    if (globalCache != null) {
                                        for (String section: getSections()) {
                                            Set<String> localIds = SuggestUtils.scanYamlPsiTreeToID(document, section, getIdsExtractor());
                                            suggest.addAll(localIds);
                                            CacheBuilder.SectionData projectIds = globalCache.get(section);
                                            suggest.addAll(projectIds.ids.keySet());
                                        }
                                    }

                                    return CachedValueProvider.Result.create(
                                            suggest,
                                            PsiModificationTracker.MODIFICATION_COUNT,
                                            ProjectRootManager.getInstance(project)
                                    );
                                }
                        );

                        for (String id : ids) {
                            resultSet.addElement(LookupElementBuilder.create(id));
                        }
                    }
                }
        );
    }
}
