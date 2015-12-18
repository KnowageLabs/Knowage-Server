package it.eng.spagobi.tools.dataset.graph;

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode()) + ((target == null) ? 0 : target.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
			
		if (obj == null)
			return false;
			
		if (getClass() != obj.getClass())
			return false;

		LabeledEdge other = (LabeledEdge) obj;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;

		if (source == null && other.source != null)
			return false;

		if (target == null && other.target != null)
			return false;

		return (source.equals(other.source) && target.equals(other.target))
				|| (source.equals(other.target) && target.equals(other.source));
	}
	
}
