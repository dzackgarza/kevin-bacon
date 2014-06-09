package New;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BFSTree<Vertex> {
    public Node<Vertex> rootNode;
    public Node<Vertex>[] children;
    public HashMap<Vertex, Node<Vertex>> treeHash;

    public BFSTree(Vertex rootVertex) {
        rootNode = new Node<Vertex>(rootVertex, null);
        rootNode.children = new ArrayList<Node<Vertex>>();
        treeHash.put(rootVertex, rootNode);
    }

    public static class Node<Vertex> {
        public Vertex data = null;
        public Node<Vertex> parent = null;
        public String movieWithParent = null;
        public List<Node<Vertex>> children = null;
        
        public Node(Vertex thisVertex, Node<Vertex> parentNode) {
        	data = thisVertex;
        	parent = parentNode;
        }
    }
    
     public void addNode(Vertex v, Vertex parent, String movie) {
    	Node<Vertex> n = treeHash.get(v);
    	Node<Vertex> p = treeHash.get(parent);
    	if (n == null) {
    		n = new Node<Vertex>(v, p);
    		p.children.add(n);
    		n.parent = p;
    		n.movieWithParent = movie;
    	}
     }
     
     public Node<Vertex> getNode(Vertex v) {
    	 Node<Vertex> r = treeHash.get(v);
    	 if (r == null)
    		 r = new Node<Vertex>(v, null);
    	 return r;
     }
}
