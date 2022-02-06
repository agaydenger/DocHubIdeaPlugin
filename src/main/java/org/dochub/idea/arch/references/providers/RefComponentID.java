package org.dochub.idea.arch.references.providers;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.dochub.idea.arch.indexing.CacheBuilder;
import org.dochub.idea.arch.indexing.CacheFileData;
import org.dochub.idea.arch.utils.PsiUtils;
import org.dochub.idea.arch.utils.VirtualFileSystemUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLSequenceItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RefComponentID extends BaseReferencesProvider {
    private static String keyword = "components";

    private class FileSourceReference extends PsiReferenceBase {

        PsiFile source;

        public FileSourceReference(@NotNull PsiElement element, PsiFile source) {
            super(element);
            this.source = source;
        }

        @Override
        public @NotNull TextRange getAbsoluteRange() {
            return super.getAbsoluteRange();
        }

        @Override
        public @Nullable PsiElement resolve() {
            return source;
        }

        @Override
        public Object @NotNull [] getVariants() {
            return super.getVariants();
        }
    }

    @Override
    public ElementPattern<? extends PsiElement> getPattern() {
        return PlatformPatterns.psiElement(YAMLKeyValue.class)
                .withParent(psi(YAMLMapping.class))
                .withSuperParent(2,
                        psi(YAMLKeyValue.class)
                                .withName(PlatformPatterns.string().equalTo(keyword))
                                .withSuperParent(2, psi(YAMLDocument.class))
                );
    }

    @Override
    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element,
                                                           @NotNull ProcessingContext context) {
        Project project = element.getManager().getProject();
        String id = PsiUtils.getText(((YAMLKeyValue)element).getKey());
        Map<String, Object> cache = CacheBuilder.getProjectCache(project);
        Map<String, Object> components = cache == null ? null : (Map<String, Object>) cache.get(keyword);
        List<PsiReference> refs = new ArrayList<>();

        if (id != null && components != null) {
            Map<String, Object> files = (Map<String, Object>) components.get(id);
            if (files != null) {
                for (String file : files.keySet()) {
                    VirtualFile vTargetFile = VirtualFileSystemUtils.findFile(
                            file.substring(project.getBasePath().length()),
                            project
                    );

                    if (vTargetFile == null) continue;

                    PsiFile targetFile = PsiManager.getInstance(element.getManager().getProject()).findFile(vTargetFile);
                    refs.add(new FileSourceReference(element, targetFile));
                }
            }
        }

        PsiReference[] result = PsiReference.EMPTY_ARRAY;

        if (refs.size() > 0) {
            result = new PsiReference[refs.size()];
            refs.toArray(result);
        };

        return result;
    }
}