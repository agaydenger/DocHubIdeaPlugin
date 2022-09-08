package org.dochub.idea.arch.completions.filters;

import com.intellij.psi.PsiElement;

import java.util.Set;
import java.util.function.BiFunction;

@FunctionalInterface
public interface IDSuggestionFilter extends BiFunction<PsiElement, Set<String>, Set<String>> {
    /**
     * Ничего не фильтрует
     */
    IDSuggestionFilter DEFAULT = (psiElement, strings) -> strings;

    /**
     *
     * @param componentToScan Компонент в котором ищем ключи
     * @param ids Список, к которому нужно применить фильтр
     * @return Список без элементов, которые уже определены
     */
    @Override
    Set<String> apply(PsiElement componentToScan, Set<String> ids);
}
