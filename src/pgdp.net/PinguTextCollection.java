package pgdp.net;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PinguTextCollection {
    private long id = 0;
    private List<PinguText> files = new ArrayList<>();

    public PinguTextCollection() {

    }

    public PinguText add(String title, String author, String text) {
        PinguText file = new PinguText(id, title, author, text);
        id++;
        files.add(file);
        return file;
    }

    public PinguText findById(long id) {
        for (PinguText file : files) {
            if (file.getId() == id) {
                return file;
            }
        }
        return null;
    }

    public List<PinguText> getAll() {
        return files;
    }

    public Map<PinguText, Double> findPlagiarismFor(long id) {
        if (this.findById(id) == null) {
            return null;
        }
        Map<PinguText, Double> result = new HashMap<>();
        if (files.isEmpty()) {
            return null;
        }
        for (PinguText file : files) {
            if (!file.equals(this.findById(id))) {
                double similarity = file.computeSimilarity(this.findById(id));
                if (similarity >= 0.001) {
                    result.put(file, similarity);
                }
            }
        }
        return result;
    }



}

