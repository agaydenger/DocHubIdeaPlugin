package org.dochub.idea.arch.completions;

import com.intellij.codeInsight.completion.*;
import org.dochub.idea.arch.completions.providers.*;
import org.dochub.idea.arch.completions.providers.aspects.AspectID;
import org.dochub.idea.arch.completions.providers.aspects.AspectLocation;
import org.dochub.idea.arch.completions.providers.components.*;
import org.dochub.idea.arch.completions.providers.contexts.*;
import org.dochub.idea.arch.completions.providers.datasets.DatasetOrigin;
import org.dochub.idea.arch.completions.providers.docs.*;
import org.dochub.idea.arch.completions.providers.forms.FormItem;
import org.dochub.idea.arch.completions.providers.forms.FormItemField;
import org.dochub.idea.arch.completions.providers.forms.FormItemFieldRequired;
import org.dochub.idea.arch.completions.providers.imports.ImportItem;
import org.dochub.idea.arch.completions.providers.namespaces.NamespaceID;
import org.dochub.idea.arch.completions.providers.technologies.ItemsItem;
import org.dochub.idea.arch.completions.providers.technologies.SectionItem;

public class YamlCompletion extends CompletionContributor {
    private static final CustomProvider[] providers = {
            new Root(),
            new Contexts(),
                new ContextID(),
                new Extralinks(),
                new Uml(),
                    new UmlNotations(),
                new ContextComponents(),
                new ContextLocation(),
            new Components(),
                new ComponentID(),
                new ComponentEntity(),
                new ComponentLinks(),
                    new ComponentLinksID(),
                    new ComponentLinksDirection(),
                new ComponentAspects(),
                new ComponentContractID(),
                new ComponentTechnologies(),
            new Forms(),
                new FormItem(),
                    new FormItemField(),
                        new FormItemFieldRequired(),
            new Aspects(),
                new AspectID(),
                new AspectLocation(),
            new Namespaces(),
                new NamespaceID(),
            new Docs(),
                new DocID(),
                new DocSource(),
                new DocSubjects(),
                new DocType(),
                new DocLocation(),
                new DocOrigin(),
            new Technologies(),
                new SectionItem(),
                new ItemsItem(),
            new Imports(),
                new ImportItem(),
            new Dataset(),
                new DatasetOrigin()
    };

    public YamlCompletion() {
        for (final CustomProvider provider : providers) {
            provider.appendToCompletion(this);
        }
    }
}
