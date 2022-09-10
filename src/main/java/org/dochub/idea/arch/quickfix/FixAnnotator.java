package org.dochub.idea.arch.quickfix;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.patterns.ElementPattern;
import com.intellij.psi.PsiElement;
import org.dochub.idea.arch.annotators.ReferencedObjectUndefinedAnnotator;
import org.dochub.idea.arch.quickfix.aspects.AspectRootQuickFix;
import org.dochub.idea.arch.quickfix.components.ComponentRootQuickFix;
import org.dochub.idea.arch.quickfix.contexts.ContextRootQuickFix;
import org.dochub.idea.arch.quickfix.docs.DocRootQuickFix;
import org.dochub.idea.arch.quickfix.namespaces.NamespaceRootQuickFix;
import org.jetbrains.annotations.NotNull;

public class FixAnnotator implements Annotator {
    private static final ReferencedObjectUndefinedAnnotator REFERENCED_OBJECT_UNDEFINED_ANNOTATOR = new ReferencedObjectUndefinedAnnotator();
    private BaseQuickFix[] fixes = {
            new ComponentRootQuickFix(),
            new AspectRootQuickFix(),
            new DocRootQuickFix(),
            new ContextRootQuickFix(),
            new NamespaceRootQuickFix()
    };

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        for (BaseQuickFix fix : fixes) {
            ElementPattern pattern = fix.getFixPattern(element);
            if (pattern.accepts(element)) {
                fix.makeFix(element, holder);
                //Применяем первый найденный
                return;
            }
        }
        REFERENCED_OBJECT_UNDEFINED_ANNOTATOR.annotate(element, holder);
    }
}
