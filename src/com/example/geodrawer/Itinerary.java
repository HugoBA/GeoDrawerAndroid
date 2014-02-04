package com.example.geodrawer;
import java.util.*;


public class Itinerary 
{
        /**
         * The name of the itinerary
         */
        private final String name;
        
        /**
         * The length of the itinerary (in km)
         */
        private final double length;
        /**
         * list of nodes corresponding to the 
         */
        
        private List<Coordinates> nodes;
        
        /*/**
         * Default constructor
         *
        public Itinerary()
        {
                this.name = "None";
                this.length = -1;
                this.nodes = null;
        }*/
        
        /**
         * Constructor based on given name, length and nodes
         * @param name corresponds to the name of the itinerary
         * @param nodes corresponds to the nodes witch composed the itinerary
         * we consider the final point coordinates is set (can equal to the start point)
         */
        public Itinerary(String name, List<Coordinates> nodes)
        {
                this.name= name;
                this.nodes=nodes;
                
                double tmpLength = 0;
                for(int i=0; i< nodes.size()-1; i++)
                {
                        tmpLength += Coordinates.getDistanceBetween2Points(nodes.get(i), nodes.get(i+1));
                }
                this.length = tmpLength;
        }
        
        /**
         * Name getter
         */
        public String getName() {
                return name;
        }

        /**
         * Length getter
         */
        public         double getLength() {
                return length;
        }

        /**
         * Nodes getter
         */
        public List<Coordinates> getNodes() {
                return nodes;
        }
        
        /**
         * Return Coordinates.toString() of all the point, separated by '\n'
         */
        public String toString()
        {
                String list = this.name+'\n';
                for(int i = 0; i < this.nodes.size(); i++)
                        list = list + this.nodes.get(i).toString()+'\n';
                return list+'\n';
        }
}
