package database;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.VCARD;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class JSONToTDB {

    private Model model;

    public JSONToTDB(String jsonPath) {

        Dataset dataset = TDBFactory.createDataset("movie_tdb");
        Model model = dataset.getDefaultModel();

        if (model.isEmpty()) {
            String jsonString = "";
            try {
                jsonString = jsonFileToString(jsonPath);
            } catch(IOException e) {
                e.printStackTrace();
            }
            System.out.println("Doing first time setup...");
            System.out.println("Converting json to TDB file");
            this.model= parseJSONString(jsonString, model);
        }
    }

    public String jsonFileToString(String pathname) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new FileReader(pathname));

        StringBuffer stringBuffer = new StringBuffer();
        String line;

        while((line =bufferedReader.readLine()) != null){
            stringBuffer.append(line).append("\n");
        }

        return stringBuffer.toString();
    }

    // This is the method that is responsible converting the JSON file into a semantic TDB file.
    public Model parseJSONString(String json, Model model) {

        JsonElement jsonElement = new JsonParser().parse(json);
        JsonArray jsonArray = jsonElement.getAsJsonArray();

        // We use mostly dbpedia.org to describe our data semantically.
        String dbo = "http://dbpedia.org/ontology/";
        String dbp = "http://dbpedia.org/page/";

        // Our extension uri that we use for the cases where we could not find a fitting property on dbpedia.
        String info216 = "http://info216.no/v2019/vocabulary/";
        String keywordURI = "http://info216.no/v2019/vocabulary/keyword#";

        String rdf = "https://www.w3.org/1999/02/22-rdf-syntax-ns#";
        Property rdfAbout = model.createProperty(rdf+"about");

        // Creating two classes - Film and Person. Actors and Directors will be members of Person.
        Resource movieClass = model.createResource(OWL.Class);
        movieClass.addProperty(rdfAbout, dbp + "Film");
        Resource personClass = model.createResource(OWL.Class);

        Resource actorClass = model.createResource(OWL.Class);
        personClass.addProperty(rdfAbout, dbp + "Person");
        actorClass.addProperty(RDFS.subClassOf, personClass);

        // This is where we parse the unsemantic moves.json and convert it into a triple store database.

        // for each movie in the json-file.
        for(JsonElement movie : jsonArray) {

            JsonObject movieObject = movie.getAsJsonObject();
            Property title = model.createProperty(info216 + "title");
            Property year = model.createProperty(dbo + "year");
            Property runtime = model.createProperty(dbo + "filmRuntime");
            Property director = model.createProperty(dbo + "director");
            Property actors = model.createProperty(info216 + "actors");
            Property actor = model.createProperty(dbo + "starring");
            Property genres = model.createProperty(info216 + "genres");
            Property keywords = model.createProperty(info216 + "keywords");
            Property keyword = model.createProperty(info216 + "keyword");
            Property gross = model.createProperty(dbo + "gross");
            Property content_rating = model.createProperty(dbo + "content_rating");
            Property country = model.createProperty(dbo + "country");
            Property language = model.createProperty(dbo + "language");
            Property num_votes = model.createProperty(info216 + "imdb_votes");
            Property imdb_rating = model.createProperty(dbo + "rating");
            Property imdb_link = model.createProperty(dbp + "Hyperlink");
            Property imdb_id = model.createProperty(info216 + "imdbId");
            Property extra_information = model.createProperty(info216 + "extra");

            // Create director resource
            Resource directorPerson = model.createResource(dbp + movieObject.get("director").getAsString())
                    .addProperty(RDF.type, dbo + "MovieDirector")
                    .addProperty(RDFS.subClassOf, personClass)
                    .addProperty(VCARD.FN, movieObject.get("director").getAsString())
                    .addProperty(VCARD.TITLE, "Director");

            Resource movieRDF = model.createResource(dbp + movieObject.get("title").getAsString())
                    .addProperty(RDF.type, dbp + "Film")
                    .addProperty(RDFS.label, movieObject.get("title").getAsString())
                    .addProperty(title, movieObject.get("title").getAsString())
                    .addProperty(director, directorPerson)
                    .addProperty(year, movieObject.get("year").getAsString(), XSDDatatype.XSDgYear)
                    .addProperty(genres, movieObject.get("genres").getAsString())
                    .addProperty(runtime, movieObject.get("runtime").getAsString(), XSDDatatype.XSDdouble)
                    .addProperty(imdb_rating, movieObject.get("imdb_rating").getAsString(), XSDDatatype.XSDdouble)
                    .addProperty(num_votes, movieObject.get("num_votes").getAsString(), XSDDatatype.XSDinteger)
                    .addProperty(gross, movieObject.get("gross").getAsString(), XSDDatatype.XSDdouble)
                    .addProperty(content_rating, movieObject.get("content_rating").getAsString())
                    .addProperty(country, movieObject.get("country").getAsString())
                    .addProperty(language, movieObject.get("language").getAsString())
                    .addProperty(imdb_id, movieObject.get("imdb_id").getAsString())
                    .addProperty(imdb_link, movieObject.get("imdb_link").getAsString());

            //model.write(System.out, "TURTLE");

            // For each movie, create an actors property that points to a blank node.
            JsonObject actorsJson = movieObject.getAsJsonObject("actors");
            Set<Map.Entry<String, JsonElement>> entrySet = actorsJson.entrySet();
            Resource actorsBlankNode = model.createResource();

            // The blank node has an actor property for each actor that stars in this movie.
            for(Map.Entry<String, JsonElement> entry : entrySet) {
                Resource actorPerson = model.createResource(dbp + actorsJson.get(entry.getKey()).getAsString())
                        .addProperty(RDF.type, dbo + "Actor")
                        .addProperty(RDFS.subClassOf, personClass)
                        .addProperty(VCARD.FN, actorsJson.get(entry.getKey()).getAsString())
                        .addProperty(VCARD.TITLE, "Actor");
                actorsBlankNode.addProperty(actor, actorPerson);

            }

            movieRDF.addProperty(actors, actorsBlankNode);

            // For each movie, create a keywords property that points to a blank node.
            JsonObject keywordsJSON = movieObject.getAsJsonObject("keywords");
            Set<Map.Entry<String, JsonElement>> keywordEntrySet = keywordsJSON.entrySet();
            Resource keywordsBlankNode = model.createResource();

            // The blank node has a keyword property for each keyword that is associated with this movie.
            for(Map.Entry<String, JsonElement> entry : keywordEntrySet) {
                Resource keywordResource = model.createResource(keywordURI + keywordsJSON.get(entry.getKey()).getAsString())
                        .addProperty(RDF.type, keywordURI)
                        .addProperty(RDFS.label, keywordsJSON.get(entry.getKey()).getAsString());
                keywordsBlankNode.addProperty(keyword, keywordResource);
            }

            movieRDF.addProperty(keywords, keywordsBlankNode);

            // Create a blank node for extra information which be added to the movies which are the output of the recommendation algorithm.
            Resource extraBlankNode = model.createResource();
            movieRDF.addProperty(extra_information, extraBlankNode);
        }
        //return the model contain all semantic triples made from the original JSON document.
        return model;
    }

    public Model getModel() {
        return this.model;
    }

}
