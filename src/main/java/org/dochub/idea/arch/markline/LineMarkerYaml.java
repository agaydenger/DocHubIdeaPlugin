package org.dochub.idea.arch.markline;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.dochub.idea.arch.indexing.CacheBuilder;
import org.dochub.idea.arch.references.providers.RefAspectID;
import org.dochub.idea.arch.references.providers.RefComponentID;
import org.dochub.idea.arch.references.providers.RefContextID;
import org.dochub.idea.arch.references.providers.RefDocsID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.dochub.idea.arch.markline.LineMarkerNavigator.DocHubNavigationHandler;
import static org.dochub.idea.arch.markline.LineMarkerNavigator.makeLineMarkerInfo;

public class LineMarkerYaml extends LineMarkerProviderDescriptor {

    @Override
    public final void collectSlowLineMarkers(@NotNull List<? extends PsiElement> elements, @NotNull Collection<? super LineMarkerInfo<?>> result) {
        for (int i = 0, size = elements.size(); i < size; i++) {
            PsiElement element = elements.get(i);
            collectNavigationMarkers(element, result);
        }
    }

    protected void collectNavigationMarkers(
            @NotNull PsiElement element,
            @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result
    ) {
    }

    @Override
    public String getName() {
        return null;
    }

    public interface ElementExplain {
        default LineMarkerNavigator.DocHubNavigationHandler register(String id) {
            return null;
        }

        ;
    }

    private LineMarkerInfo explainElement(@NotNull PsiElement element, ElementExplain explain, String cacheName) {
        LineMarkerInfo result = null;
        String id = null;
        PsiElement markElement = element;
        if (element instanceof YAMLKeyValue) {
            markElement = element.getFirstChild();
            id = ((YAMLKeyValue) element).getName();
        } else if (element instanceof YAMLPlainTextImpl) {
            markElement = element.getFirstChild();
            id = element.getText();
        }
        if (id != null && isRegisteredStructure(cacheName, id, element.getProject())) {
            result = makeLineMarkerInfo(
                    explain.register(id),
                    markElement
            );
        }
        return result;
    }

    private boolean isRegisteredStructure(String cacheName, String id, Project project) {
        return Optional.ofNullable(CacheBuilder.getProjectCache(project))
                .map(c -> c.get(cacheName))
                .map(c -> c.ids.get(id))
                .isPresent();
    }

    private LineMarkerInfo getLineMarkerInfoForComponent(@NotNull PsiElement element) {
        return explainElement(element, new ElementExplain() {
            @Override
            public DocHubNavigationHandler register(String id) {
                return new DocHubNavigationHandler("component", id);
            }
        }, "components");
    }

    private LineMarkerInfo getLineMarkerInfoForDocument(@NotNull PsiElement element) {
        return explainElement(element, new ElementExplain() {
            @Override
            public DocHubNavigationHandler register(String id) {
                return new DocHubNavigationHandler("document", id);
            }
        }, "docs");
    }

    private LineMarkerInfo getLineMarkerInfoForAspect(@NotNull PsiElement element) {
        return explainElement(element, new ElementExplain() {
            @Override
            public DocHubNavigationHandler register(String id) {
                return new DocHubNavigationHandler("aspect", id);
            }
        }, "aspects");
    }

    private LineMarkerInfo getLineMarkerInfoForContext(@NotNull PsiElement element) {
        return explainElement(element, new ElementExplain() {
            @Override
            public DocHubNavigationHandler register(String id) {
                return new DocHubNavigationHandler("context", id);
            }
        }, "contexts");
    }

    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        LineMarkerInfo result = null;
        if (RefComponentID.pattern().accepts(element)) {
            result = getLineMarkerInfoForComponent(element);
        } else if (RefDocsID.pattern().accepts(element)) {
            result = getLineMarkerInfoForDocument(element);
        } else if (RefAspectID.pattern().accepts(element)) {
            result = getLineMarkerInfoForAspect(element);
        } else if (RefContextID.pattern().accepts(element)) {
            result = getLineMarkerInfoForContext(element);
        }
        return result;
    }
}
