package projects.topologyControl1.nodeDefinitions.routing.fuzzy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import projects.topologyControl1.comparators.FslComparator;
import projects.topologyControl1.enumerators.Order;
import projects.topologyControl1.nodeDefinitions.BasicNode;
import sinalgo.nodes.Node;

public class FuzzyRoutingEntry {
	private List<RoutingField> fields = new ArrayList<RoutingField>();
	
	public FuzzyRoutingEntry (Integer seq, Integer numHops, Node nextHop, Boolean active, Integer index){
		addField(seq, numHops, nextHop, active, index);
				
	}
	
	public void addField(Integer seq, Integer numHops, Node nextHop, Boolean active, Integer index){
		RoutingField r = new RoutingField(seq, numHops, nextHop, active, index);
		fields.add(r);		
	}
	
	/**
	 * Get the route with the first route with the highest fsl
	 * Routes with lower fsl are best
	 * 
	 * @return
	 */
	public Node getFirstActiveRoute(){		
		
		Node node = null;
		Collections.sort(fields, new FslComparator(Order.DESC));
		
		RoutingField rf = fields.get(0);
		
		node = rf.getNextHop();
		
		return node;
	}
	
	/**
	 * Get the route given an indice.
	 * Routes with lower fsl are best
	 * 
	 * @return
	 */
	public Node getActiveRoute(Integer ind){		
		
		Node node = null;
		Collections.sort(fields, new FslComparator(Order.DESC));
		
		RoutingField rf = fields.get(ind);
		
		node = rf.getNextHop();
		
		return node;
	}
	
	public boolean removeEntry(RoutingField rf){
		return fields.remove(rf);
	}
	
	public Integer getFieldsSize(){
		return fields.size();
	}
	
	public Node getBestRoute(Node node){
		return getFirstActiveRoute();
	}
	
	public Boolean containsNodeInNextHop(Node node){
		Boolean result = false;
		
		for (RoutingField field : fields){
			if (field.getNextHop().equals(node)){
				result = true;
				break;
			}
		}
		return result;
	}
	
	public RoutingField getRoutingField(Node node){
		RoutingField rf = null;
		for (RoutingField field : fields){
			if (field.getNextHop().equals(node)){
				rf = field;
			}
		}
		return rf;
	}
	
	/**
	 * This method search the route with the lowest fsl and exchange by a new one
	 * Routes with high fsl are better
	 * 
	 * @param seq
	 * @param numHops
	 * @param nextHop
	 * @param active
	 * @param fsl
	 */
	public Boolean exchangeRoute(Integer seq, Integer numHops, BasicNode nextHop, Boolean active, Double fsl, Integer index){
		
		Boolean result = Boolean.FALSE;
		
		RoutingField r = new RoutingField(seq, numHops, nextHop, active, index);
		
		//
		Collections.sort(fields, new FslComparator(Order.ASC));
		
		RoutingField rOld = fields.get(0);
		
		if (r.getFsl().compareTo(rOld.getFsl()) > 0){
			fields.remove(0);
			fields.add(r);
			result = Boolean.TRUE;
		}
		
		return result;
		
	}
	
	public Double getLowestFsl(){
		Collections.sort(fields, new FslComparator(Order.ASC));
		RoutingField rf = fields.get(0);
		
		return rf.getFsl();
	}
	
	public Double getHighestFsl(){
		Collections.sort(fields, new FslComparator(Order.DESC));
		RoutingField rf = fields.get(0);
		
		return rf.getFsl();
	}
	
	public List<RoutingField> getRoutingFields(){
		return fields;
	}
	
	public Boolean hasRouteWithIndex(Integer index){
		Boolean result = Boolean.FALSE;
		
		for (RoutingField f : fields){
			if (f.getIndex().equals(index)){
				result = Boolean.TRUE;
				break;
			}
		}
		
		return result;
	}
	
	public RoutingField getRouteWithIndexAndNode(Integer index, Node node){
		RoutingField result = null;
		
		for (RoutingField f : fields){
			if (f.getIndex().equals(index) && f.getNextHop().equals(node)){
				result = f;
				break;
			}
		}
		
		return result;
	}
	
	public RoutingField getRouteWithIndex(Integer index){
		RoutingField result = null;
		
		for (RoutingField f : fields){
			if (f.getIndex().equals(index)){
				result = f;
				break;
			}
		}
		
		return result;
	}
	
	public RoutingField getFirstRoutingEntry(){
		RoutingField result = null;
		
		result = fields.get(0);
		
		return result;
	}
	
	/**
	 * Returns all next hops
	 * @return all nexts hops
	 */
	public List<Integer> getAllNextHops(){
		List<Integer> result = new ArrayList<Integer>();
			
		for (RoutingField f : fields){
			result.add(f.getNextHop().ID);
		}
		
		return result;
	}
}
