package projects.topologyControl1.nodeDefinitions.energy;

public interface IEnergy {
	public void spend(EnergyMode mode);
	
	public Float getTotalSpentEnergy();
	
	public Float getEnergy();
	
	/**
	 * This method returns the initial energy of the node.
	 * @return InitialEnergy
	 */
	public Float getInitialEnergy();
}
