package org.dochub.idea.arch.utils;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SuggestUtils {
    public static List<String> scanDirByContext(String basePath, String context, String[] extensions) {
        List<String> result = new ArrayList<String>();
        String prefix =
                context.startsWith("../") ? context.substring(3) :
                        context.equals(".") || context.equals("..") ? "" : context;
        if (context.endsWith("/.") || context.equals(".")) {
            result.add(prefix + "./");
        } else if (context.endsWith("/..") ||  context.equals("..")) {
            result.add(prefix + "/");
        } else {
            String dirName =
                    context.endsWith("/") || context.length() == 0
                            ? context
                            : (new File(context)).getParent() + "/";
            ;
            File dir = new File(basePath + "/" + dirName);
            File[] listFiles = dir.listFiles();
            if (listFiles != null) {
                for (File f : listFiles) {
                    String suggest = (context.startsWith("../") ? dirName.substring(3) : dirName)
                            + f.getName();
                    if (f.isDirectory()) {
                        result.add(suggest + "/");
                    } else for (String ext : extensions) {
                        if (f.getName().endsWith(ext)) {
                            result.add(suggest);
                        }
                    }
                }
            }
        }
        return result;
    }

    public static void appendDividerItem(List<String> list, String item, String context, String divider) {
        String[] contextParts = (context + " ").split(new String("\\" + divider));
        String[] itemParts = item.split(new String("\\" + divider));
        if (item.startsWith(context) && item.length() > context.length()) {
            String suggest = contextParts.length > 0 ? String.join(divider,
                    Arrays.copyOfRange(contextParts, 0 , contextParts.length - 1)
            ) : "";

            suggest +=  (suggest.length() > 0 ? divider : "") + itemParts[contextParts.length - 1];

            if (list.indexOf(suggest) < 0)
                list.add(suggest);
        }
    }

    public static List<String> scanYamlStreamToID(InputStream stream, String section, String context) {
        List<String> result = new ArrayList<>();
        Yaml yml = new Yaml();
        Map<String, Object> document = yml.load(stream);
        if (document != null) {
            for (Map.Entry<String, Object> entry : document.entrySet()) {
                if (entry.getKey().equals(section)) {
                    Map<String, Object> components = (Map<String, Object>) entry.getValue();
                    for (Map.Entry<String, Object> component : components.entrySet()) {
                        appendDividerItem(result, component.getKey(), context,".");
                    }
                }
            }
        }

        return result;
    }

    public static @NotNull Set<String> scanYamlPsiTreeToID(@NotNull PsiElement document, @NotNull String section,@NotNull Function<PsiElement, Set<String>> idsExtractor) {
        return PsiTreeUtil.getChildrenOfTypeAsList(document.getFirstChild(), YAMLKeyValue.class).stream()
                .filter(kv -> section.equals(kv.getKeyText()))
                .map(PsiElement::getLastChild)
                .flatMap(v -> idsExtractor.apply(v).stream())
                .collect(Collectors.toSet());
    }

    public static List<String> scanYamlPsiTreeToLocation(PsiElement element, String section) {
        PsiElement document = element;
        List<String> result = new ArrayList<>();
        while (document != null) {
            if (ObjectUtils.tryCast(document,  YAMLDocument.class) != null)
                break;
            document = document.getParent();
        }
        if (document != null) {
            PsiElement[] yamlSections = document.getFirstChild().getChildren();
            // Обход корневых секций
            for (PsiElement yamlSection : yamlSections) {
                YAMLKeyValue yamlKey = ObjectUtils.tryCast(yamlSection,  YAMLKeyValue.class);
                if (yamlKey != null && PsiUtils.getText(yamlKey.getKey()).equals(section)) {
                    // Обход нужной секции
                    PsiElement[] yamlIDs = yamlSection.getLastChild().getChildren();
                    for (PsiElement id : yamlIDs ) {
                        YAMLKeyValue yamlID = ObjectUtils.tryCast(id,  YAMLKeyValue.class);
                        if (yamlID != null) {
                            // Обход полей
                            PsiElement[] fields = id.getLastChild().getChildren();
                            for (PsiElement field : fields) {
                                YAMLKeyValue yamlField = ObjectUtils.tryCast(field,  YAMLKeyValue.class);
                                // Если нашли поле location
                                String key = PsiUtils.getText(yamlField.getKey());
                                String location = PsiUtils.getText(yamlField.getValue());
                                if (yamlField != null && key.equals("location") && location.length() > 0) {
                                    // appendDividerItem(result, PsiUtils.getText(field.getLastChild()), context, "/");
                                    result.add(PsiUtils.getText(yamlField.getValue()));
                                }
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    public static List<String> scanYamlStringToID(String data, String section, String context) {
        return scanYamlStreamToID(new ByteArrayInputStream(data.getBytes()), section, context);
    }

    public static List<String> scanYamlFileToID(String path, String section, String context) throws FileNotFoundException {
        return scanYamlStreamToID(new FileInputStream(path), section, context);
    }
}
