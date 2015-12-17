package it.eng.spagobi.tools.graph;

import org.jgrapht.graph.DefaultEdge;

public class LabeledEdge<V> extends DefaultEdge {
	private V source;
    private V target;
    private String label;
    
    public LabeledEdge(V source, V target, String label) {
        this.source = source;
        this.target = target;
        this.label = label;
    }

    public V getSource() {
        return source;
    }

    public V getTarget() {
        return target;
    }

    public String getLabel() {
        return label;
    }
}
