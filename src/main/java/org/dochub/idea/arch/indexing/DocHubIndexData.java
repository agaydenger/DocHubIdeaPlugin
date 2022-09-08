package org.dochub.idea.arch.indexing;


import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class DocHubIndexData extends HashMap<String, DocHubIndexData.Section> {

    public class Section {
        public ArrayList<String> locations = new ArrayList();
        public ArrayList<String> ids = new ArrayList();
        public ArrayList<String> imports = new ArrayList<>();
        public boolean isEmpty() {
            return (locations.size() + ids.size() + imports.size()) == 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Section section = (Section) o;
            return Objects.equals(locations, section.locations) && Objects.equals(ids, section.ids) && Objects.equals(imports, section.imports);
        }

        @Override
        public int hashCode() {
            return Objects.hash(locations, ids, imports);
        }
    }

    public void stringify(DataOutput out) throws IOException {
        out.writeInt(this.size());
        for (String secKey : this.keySet()) {
            byte[] secKeyBin = secKey.getBytes(StandardCharsets.UTF_8);
            out.writeInt(secKeyBin.length);
            out.write(secKeyBin);

            Section section = this.get(secKey);

            // Идентификаторы
            out.writeInt(section.ids.size());
            for (int i = 0; i < section.ids.size(); i++) {
                byte[] idBin = section.ids.get(i).getBytes(StandardCharsets.UTF_8);
                out.writeInt(idBin.length);
                out.write(idBin);
            }

            // Локации
            out.writeInt(section.locations.size());
            for (int i = 0; i < section.locations.size(); i++) {
                byte[] locationBin = section.locations.get(i).getBytes(StandardCharsets.UTF_8);
                out.writeInt(locationBin.length);
                out.write(locationBin);
            }

            // Импорты
            out.writeInt(section.imports.size());
            for (int i = 0; i < section.imports.size(); i++) {
                byte[] importBin = section.imports.get(i).getBytes(StandardCharsets.UTF_8);
                out.writeInt(importBin.length);
                out.write(importBin);
            }
        }
    }

    public void parse(DataInput in) throws ClassNotFoundException, IOException {
        int secCount = in.readInt();
        for (long i = 0; i < secCount; i++) {
            byte[] secKeyBin = new byte[in.readInt()];
            in.readFully(secKeyBin);
            String secKey = new String(secKeyBin, StandardCharsets.UTF_8);

            Section section = new Section();

            int idsCount = in.readInt();
            for (long n = 0; n < idsCount; n++) {
                byte[] idBin = new byte[in.readInt()];
                in.readFully(idBin);
                String id = new String(idBin, StandardCharsets.UTF_8);
                section.ids.add(id);
            }

            int locationsCount = in.readInt();
            for (long n = 0; n < locationsCount; n++) {
                byte[] locationBin = new byte[in.readInt()];
                in.readFully(locationBin);
                String location = new String(locationBin, StandardCharsets.UTF_8);
                section.locations.add(location);
            }

            int importsCount = in.readInt();
            for (long n = 0; n < importsCount; n++) {
                byte[] importBin = new byte[in.readInt()];
                in.readFully(importBin);
                String import_ = new String(importBin, StandardCharsets.UTF_8);
                section.imports.add(import_);
            }

            if (!section.isEmpty()) this.put(secKey, section);
        }
    }

    public void makeCacheDataImports(Map<String, Object> yaml) {
        ArrayList<String> result = (ArrayList<String>) yaml.get("imports");
        if ((result != null) && (result.size() > 0)) {
            Section section = new Section();
            section.imports = result;
            this.put("imports", section);
        }
    }

    public void makeCacheDataSection(Map<String, Object> yaml, String section) {
        Section secData = new Section();
        try {
            Map<String, Object> keys = (Map<String, Object>) yaml.get(section);
            if (keys != null) {
                for ( String id : keys.keySet()) {
                    secData.ids.add(id);
                    Object object = yaml.get(id);
                    if (object instanceof Map) {
                        Object location = ((Map) object).get("location");
                        if (location instanceof String) {
                            if (location != null) secData.locations.add((String) location);
                        }
                    }
                }
            }
            if ((secData.locations.size()) > 0 || (secData.ids.size() > 0) )
                this.put(section, secData);
        } catch (ClassCastException e) {}
    }

    public void makeTechnologiesCache(Map<String, Object> yaml) {
        Section secData = new Section();
        try {
            Map<String, Object> content = (Map<String, Object>) yaml.get("technologies");
            if (content != null) {
                Set<String> technologies = Optional.ofNullable(content.get("items"))
                        .map(i -> (Map<String, Object>) i)
                        .map(Map::keySet)
                        .orElseGet(Collections::emptySet);
                secData.ids.addAll(technologies);
                this.put("technologies", secData);
            }
        } catch (ClassCastException e) {}
    }

    public void makeCacheDataManifest(PsiFile file) {
        VirtualFile vFile = file.getVirtualFile();
        if (vFile != null) {
            String path = vFile.getPath();
            try {
                InputStream inputStream = new FileInputStream(path);
                Yaml yaml = new Yaml();
                Map<String, Object> sections = yaml.load(inputStream);
                if (sections != null) {
                    makeCacheDataImports(sections);
                    makeCacheDataSection(sections, "components");
                    makeCacheDataSection(sections, "aspects");
                    makeCacheDataSection(sections, "contexts");
                    makeCacheDataSection(sections, "docs");
                    makeCacheDataSection(sections, "datasets");
                    makeTechnologiesCache(sections);
                }
            } catch (Exception e) {}
        }
    }

    public DocHubIndexData(PsiFile file) {
        makeCacheDataManifest(file);
    }

    public DocHubIndexData(DataInput in) throws IOException, ClassNotFoundException {
        parse(in);
    }
}
