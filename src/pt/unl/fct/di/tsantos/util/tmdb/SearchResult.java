package pt.unl.fct.di.tsantos.util.tmdb;

import java.io.Serializable;
import java.net.URL;

public class SearchResult implements Serializable {

    protected String movie;
    protected String movieID;
    protected URL imgURL;
    protected URL url;
    protected String imdbID;

    public SearchResult(String movie, String movieID, URL imgURL,
            URL url, String imdbID) {
        this.movie = movie;
        this.movieID = movieID;
        this.imgURL = imgURL;
        this.url = url;
        this.imdbID = imdbID;
    }

    public String getImdbID() {
        return imdbID;
    }

    public void setImdbID(String imdbID) {
        this.imdbID = imdbID;
    }

    public URL getImgURL() {
        return imgURL;
    }

    public void setImgURL(URL imgURL) {
        this.imgURL = imgURL;
    }

    public String getMovie() {
        return movie;
    }

    public void setMovie(String movie) {
        this.movie = movie;
    }

    public String getMovieID() {
        return movieID;
    }

    public void setMovieID(String movieID) {
        this.movieID = movieID;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String toExtendedString() {
        return "SearchResult[" + movie + ", " + movieID +
                ", " + imgURL + ", " + url + ", " + imdbID + "]";
    }

    @Override
    public String toString() {
        return movie;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((movieID == null) ? 0 : movieID.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SearchResult other = (SearchResult) obj;
        if (movieID == null) {
            if (other.movieID != null) {
                return false;
            }
        } else if (!movieID.equals(other.movieID)) {
            return false;
        }
        return true;
    }
}
