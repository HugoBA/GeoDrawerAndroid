package com.example.geodrawer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.DefaultInfoWindow;
import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.InfoWindow;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.jdom2.*;

public class MainActivity extends Activity {

	//
	private MapView myOpenMapView;
	private IMapController myMapController;

	private MyLocationNewOverlay myLocationOverlay = null;
	private ItemizedOverlayWithBubble<ExtendedOverlayItem> userLocationOverlay = null;
	private List<Itinerary> patterns = null;
	private GeoPoint currentPosition;
	final private int MAX_PATTERN_DISTANCE = 1000;

	ArrayList<OverlayItem> items;

	
	/** Called when the activity is first created. */
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//init the map view
		initMapView();
		
		//retrieve the position of the user via the gps
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				10000, 5, new MyLocationListener(getApplicationContext(), this));
		Location location = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		//if the gps has no information about user, use the localisation of its network
		if (location == null)
			location = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

		//refresh the map with the new user location
		updateLocation(location);
		
		//now let's retrieve the hardcoded patterns from the XML file
		RetreivePatterns reception = new RetreivePatterns();
		Document xmlFile = null;
		try {
			xmlFile = reception.execute(
					"http://modmy5.com/geodrawer/patterns.xml").get();
		} catch (InterruptedException e) {
			//
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		if (xmlFile == null)
			Toast.makeText(this, reception.getException().toString(),
					Toast.LENGTH_LONG).show();
		else {
			this.patterns = parseXML(xmlFile);
		}

		//and display the patterns next to the user
		displayNearPatterns();
		
		//click on the "home" button listener
		Button button = (Button) findViewById(R.id.gohome);
		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				myLocationOverlay.disableFollowLocation();
				myLocationOverlay.disableMyLocation();
			}
		});
	}
	
	/**
	 * Init the map view
	 * load the tiles, set the global parameters
	 */
	public void initMapView()
	{
		myOpenMapView = (MapView) findViewById(R.id.openmapview);
		myOpenMapView.setTileSource(TileSourceFactory.CYCLEMAP);
		myOpenMapView.setBuiltInZoomControls(true);
		myOpenMapView.setMultiTouchControls(true);
		
		myMapController = myOpenMapView.getController();
		myMapController.setZoom(20);

		myLocationOverlay = new MyLocationNewOverlay(this, myOpenMapView);
		myOpenMapView.getOverlays().add(myLocationOverlay);
	}
	
	/**
	 * display the patterns present in the area on the map
	 */
	public void displayNearPatterns() 
	{
		List<Itinerary> nearPatterns = checkPatterns();
		Toast.makeText(this,nearPatterns.size() + " patterns found around you", Toast.LENGTH_LONG).show();
		
		for (int i = 0; i < nearPatterns.size(); i++) 
		{
			//let's create an overlay for each pattern to display them on the map
			RoadManager roadManager = new OSRMRoadManager();

			ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
			for (int j = 0; j < nearPatterns.get(i).getNodes().size(); j++)
				waypoints.add(new GeoPoint(nearPatterns.get(i).getNodes().get(j).getLatitude(), 
						nearPatterns.get(i).getNodes().get(j).getLongitude()));

			Road road = roadManager.getRoad(waypoints);
			PathOverlay roadOverlay = RoadManager.buildRoadOverlay(road,Color.rgb(255, 126, 0), 6, myOpenMapView.getContext());
			myOpenMapView.getOverlays().add(roadOverlay);

			final ArrayList<ExtendedOverlayItem> roadItems = new ArrayList<ExtendedOverlayItem>();
			Drawable marker = getResources().getDrawable(R.drawable.marker_node);

			ExtendedOverlayItem nodeMarker = new ExtendedOverlayItem(nearPatterns.get(i).getName(), "", 
					waypoints.get(getNearestNodeOfAPattern(nearPatterns.get(i))), this);

			//now create a bubble on the nearest node of the pattern to introduce the latter to the user
			InfoWindow mBubble = new CustomInfoWindow(this, myOpenMapView);
			nodeMarker.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
			nodeMarker.setMarker(marker);
			nodeMarker.setDescription("Gagnez 8 points !");
			nodeMarker.setSubDescription("A " + getDistanceFromAPattern(nearPatterns.get(i)) + "m de vous");
			nodeMarker.showBubble(mBubble, myOpenMapView, true);
			roadItems.add(nodeMarker);

			//now let's make an overlay to make a link between the pattern and the user location
			ItemizedOverlayWithBubble<ExtendedOverlayItem> roadNodes = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(this, roadItems, myOpenMapView);
			myOpenMapView.getOverlays().add(roadNodes);

			RoadManager linkToPatternManager = new MapQuestRoadManager("Fmjtd%7Cluur29082g%2C2x%3Do5-90z2lw");
			linkToPatternManager.addRequestOption("routeType=fastest");
			ArrayList<GeoPoint> linkToPatternWaypoints = new ArrayList<GeoPoint>();
			linkToPatternWaypoints.add(this.currentPosition);
			linkToPatternWaypoints.add(new GeoPoint(nearPatterns.get(i)
					.getNodes().get(0).getLatitude(), nearPatterns.get(i)
					.getNodes().get(0).getLongitude()));
			Road linkToPattern = linkToPatternManager.getRoad(linkToPatternWaypoints);
			PathOverlay linkToPatternOverlay = RoadManager.buildRoadOverlay(
					linkToPattern, Color.rgb(170, 170, 170), 4,
					myOpenMapView.getContext());
			myOpenMapView.getOverlays().add(linkToPatternOverlay);

			//center the map on the pattern
			myMapController.setCenter(new GeoPoint(nearPatterns.get(i)
					.getNodes().get(0).getLatitude(), nearPatterns.get(i)
					.getNodes().get(0).getLongitude()));
		}
		//refresh the map with all the modifications
		myOpenMapView.invalidate();

	}

	public void updateLocation(Location location) 
	{
		if (location != null) {
			this.currentPosition = new GeoPoint(location.getLatitude(),
					location.getLongitude());
			TextView latText = (TextView) findViewById(R.id.latitude);
			latText.setText("Latitude : "
					+ Double.toString(location.getLatitude()));

			TextView longitText = (TextView) findViewById(R.id.longitude);
			longitText.setText("Longitude : "
					+ Double.toString(location.getLongitude()));
			
			ArrayList<ExtendedOverlayItem> userNodeItems = new ArrayList<ExtendedOverlayItem>();
			Drawable userMarker = getResources().getDrawable(R.drawable.marker_via);

			ExtendedOverlayItem userNodeMarker = new ExtendedOverlayItem("Hey You !",
					"", this.currentPosition, this);
			userNodeMarker.setDescription("There are 2 patterns around you.");
			userNodeItems.add(userNodeMarker);
			userNodeMarker.setMarker(userMarker);
			myMapController.setCenter(this.currentPosition);
			
			//we update the user location overlay
			if(this.userLocationOverlay != null)
			{
				myOpenMapView.getOverlays().remove(myOpenMapView.getOverlays().indexOf(this.userLocationOverlay));
				
					Toast.makeText(this, "index:"+
							Integer.toString(myOpenMapView.getOverlays().indexOf(this.userLocationOverlay)),
							Toast.LENGTH_LONG).show();
			}

			this.userLocationOverlay = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(
					this, userNodeItems, myOpenMapView);
			myOpenMapView.getOverlays().add(this.userLocationOverlay);
		}
	}

	static List<Itinerary> parseXML(Document xmlFile) {
		Element racine = xmlFile.getRootElement();
		String patternName, position;
		List<Coordinates> patternCoords;
		List<Itinerary> patterns = new LinkedList<Itinerary>();

		List<Element> listPatterns = racine.getChildren("pattern");
		Iterator<Element> i = listPatterns.iterator();

		while (i.hasNext()) {
			Element current = (Element) i.next();

			patternName = current.getChild("name").getText();

			Element nodes = current.getChild("nodes");
			List<Element> listNodes = nodes.getChildren("node");

			Iterator<Element> j = listNodes.iterator();
			patternCoords = new LinkedList<Coordinates>();

			while (j.hasNext()) {
				Element currentNode = (Element) j.next();
				position = currentNode.getText();

				patternCoords.add(new Coordinates(Double.parseDouble(position
						.split(",")[0]),
						Double.parseDouble(position.split(",")[1])));
			}
			patterns.add(new Itinerary(patternName, patternCoords));
		}
		return patterns;
	}

	/**
	 * This method checks if there is a pattern present in the area
	 * 
	 * @return
	 */
	public List<Itinerary> checkPatterns() {
		List<Itinerary> result = new LinkedList<Itinerary>();
		Itinerary currentPattern;

		Iterator<Itinerary> iter = this.patterns.iterator();
		while (iter.hasNext()) {
			currentPattern = iter.next();
			if (getDistanceFromAPattern(currentPattern) < this.MAX_PATTERN_DISTANCE)
				result.add(currentPattern);
		}
		return result;
	}

	/**
	 * get the distance when the current user location and the nearest node of a pattern
	 * @param pattern
	 * @return the distance to the nearest node 
	 */
	public double getDistanceFromAPattern(Itinerary pattern) {
		double distance, minDistance = this.MAX_PATTERN_DISTANCE;
		for (int i = 0; i < pattern.getNodes().size(); i++) {
			distance = Coordinates.getDistanceBetween2Points(
					new Coordinates(this.currentPosition.getLatitude(),
							this.currentPosition.getLongitude()), pattern
							.getNodes().get(i));
			if (distance < minDistance)
				minDistance = distance;
		}
		return minDistance;
	}

	/**
	 * method to know which node of a pattern is the nearest to the user
	 * @param pattern
	 * @return the nearest node
	 */
	public int getNearestNodeOfAPattern(Itinerary pattern) {
		double distance, minDistance = this.MAX_PATTERN_DISTANCE;
		int nodeIndex = 0;
		for (int i = 0; i < pattern.getNodes().size(); i++) {
			distance = Coordinates.getDistanceBetween2Points(
					new Coordinates(this.currentPosition.getLatitude(),
							this.currentPosition.getLongitude()), pattern
							.getNodes().get(i));
			if (distance < minDistance) {
				minDistance = distance;
				nodeIndex = i;
			}
		}
		return nodeIndex;
	}
	
	/**
	 * method executed when the users clicks on a pattern pop-up
	 */
	public void patternClicked()
	{
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setMessage("Do you want to accept this challenge ?");

		builder.setPositiveButton("YES", new AcceptPatternListener(getApplicationContext() ));

		builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		AlertDialog alert = builder.create();
		alert.show(); 
	}
	
	/** 
	 * actions on resume
	 */
	@Override
	protected void onResume() {
		super.onResume();
		myLocationOverlay.disableFollowLocation();
		myLocationOverlay.disableMyLocation();
		
	}

	/**
	 * actions on app pause
	 */
	@Override
	protected void onPause() {
		super.onPause();
		myLocationOverlay.disableMyLocation();
		myLocationOverlay.disableFollowLocation();
		
	}
}
