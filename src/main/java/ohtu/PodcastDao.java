package ohtu;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PodcastDao {

    private String tietokantaosoite;

    public PodcastDao(String tietokantaosoite) {
        this.tietokantaosoite = tietokantaosoite;
    }

    public boolean lisaaPodcast(String url, String tekija, String kuvaus) throws Exception {
        if (valid(url, tekija)) {
            Podcast podcast = new Podcast(url, tekija, "0", kuvaus);
            Connection conn = DriverManager.getConnection(tietokantaosoite);
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO Podcast(url, tekija, luettu, kuvaus) "
                    + "VALUES ( ?, ?, ?, ?)");
            stmt.setString(1, podcast.getUrl());
            stmt.setString(2, podcast.getTekija());
            stmt.setString(3, podcast.getLuettu());
            stmt.setString(4, podcast.getKuvaus());
            stmt.execute();
            stmt.close();
            conn.close();
            return true;
        }
        return false;

    }

    public List<Podcast> haePodcastit() throws Exception {
        List<Podcast> podcastit = new ArrayList();

        Connection conn = DriverManager.getConnection(tietokantaosoite);
        ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM Podcast");

        while (rs.next()) {
            String id = rs.getString("id");
            String url = rs.getString("url");
            String tekija = rs.getString("tekija");
            String onkoLuettu = rs.getString("luettu");
            String kuvaus = rs.getString("kuvaus");

            Podcast p = new Podcast(url, tekija, onkoLuettu, kuvaus);
            p.setId(id);

            podcastit.add(p);
        }

        rs.close();
        conn.close();

        return podcastit;
    }

    public Podcast haePodcast(String id) throws Exception {
        Connection conn = DriverManager.getConnection(tietokantaosoite);
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Podcast WHERE id = ?");
        stmt.setString(1, id);
        ResultSet rs = stmt.executeQuery();

        Podcast podcast = new Podcast(rs.getString("url"), rs.getString("tekija"), rs.getString("luettu"), rs.getString("kuvaus"));
        podcast.setId(id);

        rs.close();
        conn.close();

        return podcast;
    }

    public void poistaPodcast(String id) throws Exception {
        Connection conn = DriverManager.getConnection(tietokantaosoite);
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM Podcast WHERE id = ?");
        stmt.setString(1, id);
        stmt.execute();
        stmt = conn.prepareStatement("DELETE FROM podcasttag WHERE podcast_id = ?");
        stmt.setString(1, id);
        stmt.execute();
        stmt.close();
        conn.close();
    }

    public boolean muokkaaPodcastia(String id, String url, String tekija, String kuvaus) throws Exception {
        if (valid(url, tekija)) {
            Connection conn = DriverManager.getConnection(tietokantaosoite);
            PreparedStatement stmt = conn.prepareStatement("UPDATE Podcast SET url = ?, tekija = ?, kuvaus = ? WHERE id = ? ");
            stmt.setString(1, url);
            stmt.setString(2, tekija);
            stmt.setString(3, kuvaus);
            stmt.setString(4, id);
            stmt.execute();
            stmt.close();
            conn.close();
            return true;
        }
        return false;
    }

    public void muutaOnkoLuettu(String onkoLuettu, String id) throws Exception {
        Connection conn = DriverManager.getConnection(tietokantaosoite);
        PreparedStatement stmt = conn.prepareStatement("UPDATE Podcast SET luettu = ? WHERE id = ? ");
        stmt.setString(1, onkoLuettu);
        stmt.setString(2, id);
        stmt.execute();
        stmt.close();
        conn.close();
    }

    private boolean valid(String url, String tekija) {
        if (url == null || tekija == null) {
            return false;
        }

        if (url.trim().isEmpty()) {
            return false;
        } else if (tekija.trim().isEmpty()) {
            return false;
        }

        return true;
    }

    public List<Podcast> haePodcastitKuunnellunPerusteella(String kuunneltu) throws Exception {
        List<Podcast> podcastit = new ArrayList();

        Connection conn = DriverManager.getConnection(tietokantaosoite);
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Podcast WHERE luettu = ?");
        stmt.setObject(1, kuunneltu);

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            if (kuunneltu.equals(rs.getString("luettu"))) {
                String id = rs.getString("id");
                String url = rs.getString("url");
                String tekija = rs.getString("tekija");
                String onkoLuettu = rs.getString("luettu");
                String kuvaus = rs.getString("kuvaus");

                Podcast podcast = new Podcast(url, tekija, onkoLuettu, kuvaus);
                podcast.setId(id);

                podcastit.add(podcast);
            }
        }

        stmt.close();
        rs.close();
        conn.close();

        return podcastit;
    }

    public List<Podcast> haeHakusanaaVastaavat(String hakusana) throws Exception {
        String haku = hakusana.toLowerCase().trim();
        List<Podcast> podcastit = new ArrayList();

        Connection conn = DriverManager.getConnection(tietokantaosoite);
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Podcast");
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            String id = rs.getString("id");
            String url = rs.getString("url");
            String tekija = rs.getString("tekija");
            String onkoLuettu = rs.getString("luettu");
            String kuvaus = rs.getString("kuvaus");

            if ((tekija != null && tekija.toLowerCase().contains(haku))
                    || (kuvaus != null && kuvaus.toLowerCase().contains(haku))
                    || (url != null && url.toLowerCase().contains(haku))) {

                Podcast podcast = new Podcast(url, tekija, onkoLuettu, kuvaus);
                podcast.setId(id);

                podcastit.add(podcast);
            }
        }

        rs.close();
        stmt.close();
        conn.close();

        return podcastit;
    }

    public void lisaaTagi(String podcastId, String tagi) throws Exception {

        Connection conn = DriverManager.getConnection(tietokantaosoite);
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Tag WHERE nimi = ?");
        stmt.setString(1, tagi);
        ResultSet rs = stmt.executeQuery();

        if (rs.isBeforeFirst()) {
            String tagid = rs.getString("id");

            stmt = conn.prepareStatement("INSERT INTO podcasttag (podcast_id, tag_id) "
                    + "VALUES ( ? , ?)");
            stmt.setString(1, podcastId);
            stmt.setString(2, tagid);
            stmt.execute();
            stmt.close();
            conn.close();
        } else {
            stmt = conn.prepareStatement("INSERT INTO Tag(nimi) "
                    + "VALUES ( ? )");
            stmt.setString(1, tagi);
            stmt.execute();
            stmt = conn.prepareStatement("INSERT INTO podcasttag (podcast_id, tag_id) "
                    + "VALUES ( ? , (SELECT id FROM Tag ORDER BY id DESC LIMIT 1))");
            stmt.setString(1, podcastId);
            stmt.execute();
            stmt.close();
            conn.close();
        }

    }

    public List<Tag> haeTagitPodcastinIdnPerusteella(String podcastId) throws Exception {
        List<Tag> tagit = new ArrayList();
        Connection conn = DriverManager.getConnection(tietokantaosoite);
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Tag WHERE Tag.id IN (SELECT tag_id FROM podcasttag WHERE podcast_id = ?)");
        stmt.setString(1, podcastId);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            String id = rs.getString("id");
            String nimi = rs.getString("nimi");
            Tag t = new Tag(nimi);
            t.setId(id);
            tagit.add(t);
        }

        rs.close();
        stmt.close();
        conn.close();

        return tagit;
    }

    public List<Podcast> kaikkiVinkitTagilla(String taginNimi) throws SQLException {
        List<Podcast> podcastit = new ArrayList();
        Connection conn = DriverManager.getConnection(tietokantaosoite);
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Podcast WHERE Podcast.id IN (SELECT podcast_id FROM podcasttag WHERE tag_id IN (SELECT id FROM Tag WHERE nimi = ? ))");
        stmt.setString(1, taginNimi);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            String id = rs.getString("id");
            String url = rs.getString("url");
            String tekija = rs.getString("tekija");
            String onkoLuettu = rs.getString("luettu");
            String kuvaus = rs.getString("kuvaus");

            Podcast p = new Podcast(url, tekija, onkoLuettu, kuvaus);
            p.setId(id);
            podcastit.add(p);
        }

        rs.close();
        stmt.close();
        conn.close();

        return podcastit;
    }
}
