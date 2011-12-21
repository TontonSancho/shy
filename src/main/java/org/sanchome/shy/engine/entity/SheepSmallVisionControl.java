package org.sanchome.shy.engine.entity;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.shapes.FixedConeCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public class SheepSmallVisionControl extends GhostControl implements PhysicsCollisionListener {

	private Map<Spatial, Long> thingsToEsquive = new HashMap<Spatial, Long>();
	private Sheep sheep;
	private VehicleControl vehicleControl;
	//private Debugger debugger;
	
	public SheepSmallVisionControl(Sheep sheep, VehicleControl vehicleControl, FixedConeCollisionShape visionShape, BulletAppState bulletAppState) {
		super(visionShape);
		this.sheep = sheep;
		this.vehicleControl = vehicleControl;
		bulletAppState.getPhysicsSpace().addCollisionListener(this);
		//debugger = Debugger.open();
	}

	public void collision(PhysicsCollisionEvent collisionEvent) {
		if (isInterrestingMe(collisionEvent)) {
			Spatial theThingIAmInterrestedIn;
			boolean itsA = false;
			if (this == collisionEvent.getObjectA()) {
				theThingIAmInterrestedIn = collisionEvent.getNodeB();
			}
			else {
				theThingIAmInterrestedIn = collisionEvent.getNodeA();
				itsA = true;
			}
			thereIsSomethingInterrestingMe(theThingIAmInterrestedIn, collisionEvent, itsA);
		}
	}

	public boolean isInterrestingMe(PhysicsCollisionEvent collisionEvent) {
		return (this == collisionEvent.getObjectA() || this == collisionEvent.getObjectB()) && 
		(vehicleControl != collisionEvent.getObjectA() && vehicleControl != collisionEvent.getObjectB());
	}
	
	public void thereIsSomethingInterrestingMe(Spatial theThingIAmInterrestedIn, PhysicsCollisionEvent collisionEvent, boolean itsA) {
		thingsToEsquive.put(theThingIAmInterrestedIn, System.currentTimeMillis());
		
		if ((itsA && collisionEvent.getObjectA() instanceof CharacterControl) || collisionEvent.getObjectB() instanceof CharacterControl )
			sheep.run();
		
		takeADirectionDecision();
		
	}
	
	private void takeADirectionDecision() {
		//debugger.clearThings();
		
		//System.out.println("Sheep decision: "+sheep);
		Vector3f myPosition = vehicleControl.getPhysicsLocation();
		Vector3f barycenter = Vector3f.ZERO.clone();
		
		
		float theWholeMass = 0.0f;
		// Computes the whole 'mass' of things
		// and clean-up too old seen things
		for(Spatial spatial : thingsToEsquive.keySet().toArray(new Spatial[]{})) {
			long whenWeSawTheSpatial = thingsToEsquive.get(spatial);
			if (System.currentTimeMillis() - whenWeSawTheSpatial > 5000L /*|| myPosition.distance(spatial.getWorldTranslation())>30.0f*/) {
				// Too old, remove it from the sheep 'memory'
				thingsToEsquive.remove(spatial);
				continue;
			}
			//System.out.println("---things distance:"+myPosition.distance(spatial.getWorldTranslation()));
			theWholeMass += (30.0f / myPosition.distance(spatial.getWorldTranslation()));
		}
		//System.out.println("   \\---  theWholemass: "+theWholeMass);
		// Mass of the forward direction
		theWholeMass += 5.0f;
		theWholeMass = FastMath.abs(theWholeMass);
		
		// Computes the barycenter
		for(Spatial spatial : thingsToEsquive.keySet().toArray(new Spatial[]{})) {
			Vector3f diff = spatial.getWorldTranslation().subtract(myPosition);
			//debugger.addThings(diff.x, diff.z);
			// Add this thing to the barycenter
			barycenter.addLocal(
				diff.mult(
					-(60.0f/(diff.length()*theWholeMass))
				)
			);
		}
		
		// Plus forward vector
		
		Vector3f diff = vehicleControl.getForwardVector(null).negate().normalizeLocal().multLocal(5.0f);
		barycenter.addLocal(
			diff.mult(-5.0f/theWholeMass)
		);
		

		//debugger.setDecision(barycenter.x, barycenter.z);
		
		//System.out.println("   \\---   barycenter: "+barycenter);
		
		Vector3f forwardVector = vehicleControl.getForwardVector(null).mult(30.0f);
		//System.out.println("   \\---forwardVector: "+forwardVector);
		//debugger.setCurrentDir(forwardVector.x, forwardVector.z);
		Vector2f forwardVector2D = new Vector2f(forwardVector.x, forwardVector.z).normalizeLocal();
		Vector2f barycenter2D = new Vector2f(barycenter.x, barycenter.z).normalizeLocal();
		
		float alpha = forwardVector2D.angleBetween(barycenter2D)/(4.0f+(vehicleControl.getLinearVelocity().length()/2.0f));
		//System.out.println("   \\---         alpha: "+alpha);
		
		float steerAlpha = FastMath.clamp(alpha, -FastMath.QUARTER_PI/2.0f, FastMath.QUARTER_PI/2.0f);
		//System.out.println("   \\---steer decision: "+steerAlpha);

		
		
		//debugger.repaint();
		
		vehicleControl.steer(-steerAlpha);
		
	}
	
	public static class Debugger extends JPanel {
		public Debugger() {
			this.setPreferredSize(new Dimension(300, 300));
		}
		public void setCurrentDir(float x, float z) {
			currentDir = new Point2D.Float(x, z);
		}
		public void setDecision(float x, float z) {
			decision = new Point2D.Float(x, z);
		}
		List<Point2D> things = new ArrayList<Point2D>();
		Point2D decision = new Point2D.Float(0f, 0f);
		Point2D currentDir = new Point2D.Float(0f, 0f);
		public void clearThings() {
			things.clear();
			
		}
		public void addThings(float x, float z) {
			things.add(new Point2D.Float(x, z));
		}
		@Override
		public void paint(Graphics g) {
			Graphics2D g2D = (Graphics2D)g;
			g2D.clearRect(0, 0, getWidth(), getHeight());
			g2D.setColor(Color.BLUE);
			g2D.drawLine(0, getHeight()/2, getWidth(), getHeight()/2);
			g2D.drawLine(getWidth()/2, 0, getWidth()/2, getHeight());
			g2D.setColor(Color.RED);
			for(Point2D pt : things) {
				g2D.drawLine(getWidth()/2, getHeight()/2, (int)pt.getX()*5 + getWidth()/2, (int)pt.getY()*5 + getHeight()/2);
			}
			g2D.setColor(Color.GREEN);
			g2D.drawLine(getWidth()/2, getHeight()/2, (int)decision.getX()*5 + getWidth()/2, (int)decision.getY()*5 + getHeight()/2);
			g2D.setColor(Color.MAGENTA);
			g2D.drawLine(getWidth()/2, getHeight()/2, (int)currentDir.getX()*5 + getWidth()/2, (int)currentDir.getY()*5 + getHeight()/2);
		}
		public static Debugger open() {
			JFrame frame = new JFrame();
			Debugger debugger = new Debugger();
			frame.setContentPane(debugger);
			frame.setPreferredSize(new Dimension(300, 300));
			frame.pack();
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			return debugger;
		}
	}
}
