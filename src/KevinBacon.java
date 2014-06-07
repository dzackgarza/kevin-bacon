import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

class KBGraph {
	
	// File containing 156,467 actors. 
	static String fileIn = "freebase_performances.csv";
	
	// Each vertex represents an actor, and has a set of edges.
	class Vertex {
		public String actorName;
	    public ArrayList<Edge> adjacentActors; // O(1) Retrieval for ArrayLists
	    public Vertex previousVertex; // Keeps track of its own contribution to the shortest path. 
	    
	    public Vertex (String name){
	    	actorName = name;
	    	adjacentActors = new ArrayList<Edge>();
	    }
	}
		
	// Edges represent two actors that have been in the same movie.
	class Edge {
		public String movie; 
		public Vertex nextVertex; 	// Each vertex has an edge, so the edge only needs to track its destination.
		
		public Edge(Vertex destination){
			nextVertex = destination;
		}
	}
	
	// Reads actors and movies from fileIn 
	public void buildGraphFromFile() {
		BufferedReader in = null;
		String line;
		
		try {
			in = new BufferedReader(new FileReader(fileIn));
			while ((line = in.readLine()) != null)
			{
				String[] d = line.split(",");
				System.out.println("Film: " + d[3] + "; Actor: " + d[1]);
			}
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}	
		}
	}
	
	/*public static void main (String[] args)
	{
		KBGraph g = new KBGraph();
		g.buildGraphFromFile();
		g.displayGraphStats();
		g.getActors();
		g.getShortestPath();
		g.displayResults();
	}*/
}
//HashMap<String, Vertex> graph = new HashMap<String, Vertex>();




