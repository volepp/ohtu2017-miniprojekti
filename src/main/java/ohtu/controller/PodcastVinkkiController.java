package ohtu.controller;

import ohtu.Podcast;
import ohtu.PodcastDao;
import ohtu.Video;
import ohtu.VideoDao;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class PodcastVinkkiController {

    private PodcastDao podcastDao;
    private String viesti; //käyttäjälle näytettävä virheviesti tai ilmoitus

    public PodcastVinkkiController() {
        this.podcastDao = new PodcastDao("jdbc:sqlite:kirjasto.db");
        this.viesti = "";
    }

    @GetMapping("/podcastvinkit")
    public String listaaPodcastVinkit(Model model) throws Exception {
        tarkistaOnkoViestia(model);
        model.addAttribute("podcastit", podcastDao.haePodcastitKuunnellunPerusteella("0"));
        model.addAttribute("kuunnellutPodcastit", podcastDao.haePodcastitKuunnellunPerusteella("1"));
        return "podcastvinkit";
    }

    @GetMapping("/podcastinlisaaminen")
    public String podcastinlisaaminen(Model model) {
        tarkistaOnkoViestia(model);
        return "podcast";
    }

    @PostMapping("/lisaapodcast")
    @ResponseBody
    public RedirectView lisaapodcast(@RequestParam(value = "url") String url, @RequestParam(value = "tekija") String tekija, @RequestParam(value = "kuvaus") String kuvaus) {
        Boolean lisatty = false;
        try {
            lisatty = podcastDao.lisaaPodcast(url, tekija, kuvaus);
        } catch (Exception ex) {
            return new RedirectView("/error");
        }
        if (lisatty) {
            viesti = "Lisätty podcast " + tekija + " url: " + url;
            return new RedirectView("/podcastvinkit");
        }
        viesti = "Podcastin tekijä tai url ei voi olla tyhjä!";
        return new RedirectView("/podcastinlisaaminen");
    }

    @GetMapping("/{id}/muokkaapodcastia")
    public String muokkaaPodcastia(Model model, @PathVariable String id) throws Exception {
        try {
            Podcast p = podcastDao.haePodcast(id);
            tarkistaOnkoViestia(model);
            model.addAttribute("podcast", p);
        } catch (Exception ex) {
            return "error";
        }
        return "muokkaa_podcastia";
    }

    @PostMapping("/{id}/muokkaa_podcastia")
    @ResponseBody
    public RedirectView muokkaaPodcastiaTietokantaan(@PathVariable String id, @RequestParam(value = "url") String url, @RequestParam(value = "tekija") String tekija, @RequestParam(value = "kuvaus") String kuvaus) {
        Boolean muokattu = false;
        try {
            muokattu = podcastDao.muokkaaPodcastia(id, url, tekija, kuvaus);
        } catch (Exception ex) {
            return new RedirectView("/error");
        }
        if (muokattu) {
            viesti = "Muokattu podcast " + tekija + " url " + url + "!";
            return new RedirectView("/podcastvinkit");
        }

        viesti = "Podcastin tekijä tai url ei voi olla tyhjä!";
        return new RedirectView("/" + id + "/muokkaapodcastia");
    }

    @GetMapping("/{id}/poistapodcast")
    public String poistaPodcastVarmistus(Model model, @PathVariable String id) throws Exception {
        try {
            Podcast p = podcastDao.haePodcast(id);
            model.addAttribute("podcast", p);
        } catch (Exception ex) {
            return "error";
        }
        return "poista_podcast";
    }

    @PostMapping("/{id}/poista_podcast")
    @ResponseBody
    public RedirectView poistaPodcast(@PathVariable String id) throws Exception {

        Podcast p = podcastDao.haePodcast(id);
        try {
            podcastDao.poistaPodcast(id);
        } catch (Exception ex) {
            return new RedirectView("/error");
        }
        viesti = "Poistettu podcast " + p.getTekija() + ", jonka url on " + p.getUrl() + ".";
        return new RedirectView("/podcastvinkit");

    }

    @GetMapping("/podcast/{id}/info")
    public String naytaInfo(Model model, @PathVariable String id) throws Exception {
        try {
            Podcast p = podcastDao.haePodcast(id);
            model.addAttribute("podcast", p);
        } catch (Exception ex) {
            return "error";
        }
        return "podcastin_infosivu";
    }

    @GetMapping("/{id}/onko_kuunneltu")
    public RedirectView merkitseOnkoKuunneltu(Model model, @PathVariable String id) throws Exception {
        try {
            Podcast p = podcastDao.haePodcast(id);
            p.merkitseLuetuksi();
            podcastDao.muutaOnkoLuettu(p.getLuettu(), id);

        } catch (Exception ex) {
            return new RedirectView("error");
        }
        return new RedirectView("/podcastvinkit");
    }

    private void tarkistaOnkoViestia(Model model) {
        if (!viesti.isEmpty()) {
            model.addAttribute("viesti", viesti);
            viesti = "";
        }
    }
}