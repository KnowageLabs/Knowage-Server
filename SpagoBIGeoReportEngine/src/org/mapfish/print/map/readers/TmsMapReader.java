/*
 * Copyright (C) 2009  Camptocamp
 *
 * This file is part of MapFish Print
 *
 * MapFish Print is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MapFish Print is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MapFish Print.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mapfish.print.map.readers;

import org.mapfish.print.RenderingContext;
import org.mapfish.print.Transformer;
import org.mapfish.print.map.renderers.TileRenderer;
import org.mapfish.print.utils.PJsonArray;
import org.mapfish.print.utils.PJsonObject;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public class TmsMapReader extends TileableMapReader {
    protected final String layer;
    private final String format;
    private final String extension;
    private  String layerName;
    private final String serviceVersion;
    private int count=0;

    protected TmsMapReader(String layer, RenderingContext context, PJsonObject params) {
        super(context, params);
        this.layer = layer;
        PJsonArray maxExtent = params.getJSONArray("maxExtent");
        PJsonArray tileSize = params.getJSONArray("tileSize");
        format = params.getString("format");
        serviceVersion = "1.0.0";
        int formatSemicolon = format.indexOf(";");
        if(formatSemicolon > 0) {
          extension = format.substring(0,formatSemicolon).trim();
        } else {
          extension = format.trim();
        }
        layerName = params.optString("layer");
        if(layerName == null){
        	layerName = params.optString("contextName");
        }
        if(layerName == null){
        	layerName = "9";
        }
        tileCacheLayerInfo = new TmsLayerInfo(params.getJSONArray("resolutions"), tileSize.getInt(0), tileSize.getInt(1), maxExtent.getFloat(0), maxExtent.getFloat(1), maxExtent.getFloat(2), maxExtent.getFloat(3), extension);
    }

    protected TileRenderer.Format getFormat() {
        return TileRenderer.Format.BITMAP;
    }

    protected void addCommonQueryParams(Map<String, List<String>> result, Transformer transformer, String srs, boolean first) {
        //not much query params for this protocol...
    }

    protected URI getTileUri(URI commonUri, Transformer transformer, float minGeoX, float minGeoY, float maxGeoX, float maxGeoY, long w, long h) throws URISyntaxException, UnsupportedEncodingException {
        float targetResolution = (maxGeoX - minGeoX) / w;
        TmsLayerInfo.ResolutionInfo resolution = tileCacheLayerInfo.getNearestResolution(targetResolution);

        long tileX =  Math.round(Math.floor((minGeoX - tileCacheLayerInfo.getMinX()) / (resolution.value * w)));
        int tileY = Math.round((minGeoY - tileCacheLayerInfo.getMinY()) / (resolution.value * h));


        StringBuilder path = new StringBuilder();
        if (!commonUri.getPath().endsWith("/")) {
            path.append('/');
        }

//       // path.append(this.serviceVersion);
//        path.append(this.layerName);
//       
//        path.append(String.format("%02d", resolution.index));
//        path.append('/').append(tileX);
//        path.append('/').append(tileY);
//        path.append('.').append(this.format);

//        
       // if(count==9 || count==16){
          path.append(String.format("%02d", resolution.index));
          path.append('/').append(tileX);
          path.append('/').append(tileY);
          path.append('.').append(this.format);
        //}
        

        count++;
        return new URI(commonUri.getScheme(), commonUri.getUserInfo(), commonUri.getHost(), commonUri.getPort(), commonUri.getPath() + path, commonUri.getQuery(), commonUri.getFragment());
    }

    protected static void create(List<MapReader> target, RenderingContext context, PJsonObject params) {
        //String layer = params.getString("layer");
        target.add(new TmsMapReader("t", context, params));
    }

    public boolean testMerge(MapReader other) {
        return false;
    }

    public boolean canMerge(MapReader other) {
        return false;
    }

    public String toString() {
        return layer;
    }
}
