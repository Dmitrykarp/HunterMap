package com.dmitrykarp.huntermap;

import android.content.Context;
import android.util.Xml;

import org.jetbrains.annotations.NotNull;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.layer.overlay.Polyline;
import org.xmlpull.v1.XmlSerializer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class JavaUtils {

    public static void saveData(Collection<Polyline> polyList, Context ctx) throws IOException {
        String filename = "DMKA.xml";

        FileOutputStream fos;
        fos = ctx.openFileOutput(filename, Context.MODE_APPEND);

        XmlSerializer serializer = Xml.newSerializer();
        serializer.setOutput(fos, "UTF-8");
        serializer.startDocument(null, Boolean.valueOf(true));
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

        serializer.startTag(null, "root");

        for(Polyline pl: polyList)
        {
            serializer.startTag(null, "polyline");
            for (LatLong lg:pl.getLatLongs()){
                serializer.startTag(null, "latlong");
                serializer.startTag(null, "latitude");
                serializer.text(String.valueOf(lg.latitude));
                serializer.endTag(null, "latitude");
                serializer.startTag(null, "longitude");
                serializer.text(String.valueOf(lg.longitude));
                serializer.endTag(null, "longitude");
                serializer.endTag(null, "latlong");
            }
            serializer.endTag(null, "polyline");
        }

        serializer.endDocument();
        serializer.flush();

        fos.close();
        System.out.println("END for save Data");
    }


    @NotNull
    public static Collection<LatLong> getPontList() {
        Collection<LatLong> zone = new ArrayList<>();

        zone.add(new LatLong(50.9853, 46.0186));
        zone.add(new LatLong(50.9950, 46.0800));
        zone.add(new LatLong(51.0028, 46.1028));
        zone.add(new LatLong(50.9906, 46.1100));
        //t5
        zone.add(new LatLong(50.9910, 46.1635));
        zone.add(new LatLong(50.9978, 46.1656));
        //t6
        zone.add(new LatLong(50.9972, 46.2425));
        zone.add(new LatLong(51.0088, 46.2483));
        //T7
        zone.add(new LatLong(51.0144, 46.2606));
        zone.add(new LatLong(51.0100, 46.2703));
        zone.add(new LatLong(51.0124, 46.2751));
        zone.add(new LatLong(51.0087, 46.2838));
        zone.add(new LatLong(51.0059, 46.2931));
        zone.add(new LatLong(51.0087, 46.2958));
        zone.add(new LatLong(51.0061, 46.2983));
        zone.add(new LatLong(51.0055, 46.3022));
        zone.add(new LatLong(51.0041, 46.3051));
        zone.add(new LatLong(50.9554, 46.3069));
        zone.add(new LatLong(50.9509, 46.3419));
        zone.add(new LatLong(50.9512, 46.3783));
        zone.add(new LatLong(50.9583, 46.4380));
        zone.add(new LatLong(50.9584, 46.4502));
        zone.add(new LatLong(50.9517, 46.4543));
        //T8
        zone.add(new LatLong(50.9667, 46.5800));
        //Т9
        zone.add(new LatLong(50.9294, 46.5797));
        zone.add(new LatLong(50.9233, 46.4707));
        zone.add(new LatLong(50.8996, 46.3902));
        zone.add(new LatLong(50.8787, 46.3889));
        zone.add(new LatLong(50.8321, 46.4108));
        zone.add(new LatLong(50.8276, 46.4181));
        zone.add(new LatLong(50.8219, 46.4183)); //Новокаменка Т10
        zone.add(new LatLong(50.8177, 46.4156));
        zone.add(new LatLong(50.8081, 46.3945));
        zone.add(new LatLong(50.8061, 46.3662));
        zone.add(new LatLong(50.8024, 46.3449));
        zone.add(new LatLong(50.8013, 46.2320));
        zone.add(new LatLong(50.7839, 46.1266));
        zone.add(new LatLong(50.7822, 46.0989));
        zone.add(new LatLong(50.7688, 46.0874));
        zone.add(new LatLong(50.7556, 46.0716));
        zone.add(new LatLong(50.7527, 46.0525));
        zone.add(new LatLong(50.7572, 46.0192));
        zone.add(new LatLong(50.7752, 45.9350));
        zone.add(new LatLong(50.7685, 45.9207));
        zone.add(new LatLong(50.7456, 45.8969));
        zone.add(new LatLong(50.7483, 45.8900));
        zone.add(new LatLong(50.7876, 45.8962));
        zone.add(new LatLong(50.8515, 46.0658));
        return zone;
    }
}
