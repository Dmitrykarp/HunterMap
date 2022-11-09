package com.dmitrykarp.huntermap;

import android.content.res.Resources;

import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.overlay.Polygon;
import org.mapsforge.map.rendertheme.XmlUtils;
import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JavaUtils {

    public static Collection<Polygon> initPolygons(Resources resources){
        Collection<Polygon> polyList = new ArrayList<>();
        polyList.addAll(getPolygonListByXml(resources.getXml(R.xml.yellow_polygons), false));
        polyList.addAll(getPolygonListByXml(resources.getXml(R.xml.red_polygons),true));
        return polyList;
    }

    private static Collection<Polygon> getPolygonListByXml(XmlPullParser xpp, Boolean isRed) {
        Collection<Polygon> polyList = new ArrayList<>();
        GraphicFactory graphicFactory = AndroidGraphicFactory.INSTANCE;

        Paint paintZoneRed = graphicFactory.createPaint();
        paintZoneRed.setStyle(Style.FILL);
        paintZoneRed.setStrokeWidth(3F);
        paintZoneRed.setColor(XmlUtils.getColor(graphicFactory, "#CCED3438"));

        Paint paintZone = graphicFactory.createPaint();
        paintZone.setStyle(Style.FILL);
        paintZone.setStrokeWidth(3F);
        paintZone.setColor(XmlUtils.getColor(graphicFactory, "#73ECEC35"));

        Paint paintZoneStroke = graphicFactory.createPaint();
        paintZoneStroke.setStyle(Style.STROKE);
        paintZoneStroke.setStrokeWidth(3F);
        paintZoneStroke.setColor(XmlUtils.getColor(graphicFactory, "#FFED3438"));

        Polygon currentPolygon = null;
        LatLong currentLatLong = null;
        boolean inPolygon = false;
        boolean inPoint = false;
        String latLong = "";
        double latitude = 0, longitude = 0;


        try {
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {

                String tagName = xpp.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if ("polygon".equalsIgnoreCase(tagName)) {
                            inPolygon = true;
                            if (isRed) {
                                currentPolygon = new Polygon(paintZoneRed, null, graphicFactory);
                            } else {
                                currentPolygon = new Polygon(paintZone, paintZoneStroke, graphicFactory);
                            }
                        } else if ("point".equalsIgnoreCase(tagName)) {
                            inPoint = true;
                        }
                        break;
                    case XmlPullParser.TEXT:
                        latLong = xpp.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        if (inPolygon) {
                            if (inPoint) {
                                if ("latitude".equalsIgnoreCase(tagName)) {
                                    latitude = Double.parseDouble(latLong);
                                } else if ("longitude".equalsIgnoreCase(tagName)) {
                                    longitude = Double.parseDouble(latLong);
                                } else if ("point".equalsIgnoreCase(tagName)) {
                                    currentLatLong = new LatLong(latitude, longitude);
                                    List<LatLong> latLongs = currentPolygon.getLatLongs();
                                    latLongs.add(currentLatLong);
                                    inPoint = false;
                                }
                            } else {
                                if ("polygon".equalsIgnoreCase(tagName)) {
                                    polyList.add(currentPolygon);
                                    inPolygon = false;
                                }
                            }
                        }
                        break;
                    default:
                }
                eventType = xpp.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return polyList;
    }

}
