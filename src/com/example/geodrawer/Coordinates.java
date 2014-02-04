package com.example.geodrawer;
/**
 * represent a GPS coordinate
 *
 */
public class Coordinates 
{
        
        /**
         * Represents the latitude of a point
         */
        private final double latitude;
        
        /**
         * Represents the longitude of a point
         */
        private final double longitude;
        
        /**
         * Default constructor
         */
        public Coordinates()
        {
                this.latitude = 0;
                this.longitude = 0;
        }
        
        /**
         * Constructor based on given latitude and longitude
         * @param inLat the latitude to set
         * @param inLong the longitude to set
         */
        public Coordinates(double inLat, double inLong)
        {
                this.latitude = inLat;
                this.longitude = inLong;
        }
        
        /**
         * Longitude getter
         */
        public double getLongitude() {
                return longitude;
        }

        /**
         * Latitude getter
         */
        public double getLatitude() {
                return latitude;
        }
        
        /**
         * Calculate the distance between two given points
         * @param ptA : The first point
         * @param ptB : The second point
         * @return : The distance betsween the two points in meters
         */
        public static double getDistanceBetween2Points(Coordinates ptA, Coordinates ptB)
        {
                double a,b,c,d,e,f,result;
                a=(Math.PI*ptA.getLatitude()/180); 
                b=(Math.PI*ptA.getLongitude()/180); 
                c=(Math.PI*ptB.getLatitude()/180);
                d=(Math.PI*ptB.getLongitude()/180);
                e=(Math.cos(a)*Math.cos(c)*Math.cos(b)*Math.cos(d)+Math.cos(a)*Math.sin(b)*Math.cos(c)*Math.sin(d)+Math.sin(a)*Math.sin(c)); 
                f=(Math.acos(e));
                result= Math.round(6371*f*1000);
                
                return result;
        }
        
        /**
         * Return the coordinates (*lat* ; *long*)
         */
        public String toString()
        {
                return "("+this.latitude+" ; "+this.longitude+")";
        }

        /**
         * Return latitude - longitude * 10 000 000
         */
        public int hashCode()
        {
                return (int) (this.latitude - this.longitude)*10000000;
        }
        /**
         * Two Coordinates are considered as equals when their
         *                 latitude and longitude match
         */
        public boolean equals(Object o)
        {
                if (o == this)
                        return true;
                else if (o == null)
                        return false;
                else
                {
                        Coordinates temp = (Coordinates) o;
                        return ((this.latitude == temp.getLatitude()) 
                                        && (this.longitude == temp.getLongitude()));
                }
        }
}