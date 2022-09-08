package org.dochub.idea.arch.completions.filters;

import com.intellij.navigation.NavigationItem;
import com.intellij.psi.PsiElement;
import org.dochub.idea.arch.utils.PsiUtils;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLSequenceItem;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Фильтр для родительского компонента типа список
 * Для родителя
 * parent:
 *   - el
 *   - el2
 * И списка предложений для подстановки
 * [el, el3]
 * Вернет [el3]
 */
public class KeysAlreadyInListFilter implements IDSuggestionFilter {
    @Override
    public Set<String> apply(PsiElement componentToScan, Set<String> strings) {
        if(!strings.isEmpty()) {
            Set<String> alreadyExists = PsiUtils.getChildrenOfClass(componentToScan.getLastChild(), YAMLSequenceItem.class)
                    .map(YAMLSequenceItem::getValue)
                    .map(PsiElement::getText)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            return strings.stream().filter(id -> !alreadyExists.contains(id)).collect(Collectors.toSet());
        }
        return strings;
    }
}
