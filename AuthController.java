package POKEDEX.demo.controller;

import POKEDEX.demo.model.User;
import POKEDEX.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        @RequestParam(value = "registered", required = false) String registered,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password.");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }
        if (registered != null) {
            model.addAttribute("message", "Account created successfully. Please log in.");
        }
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String email,
                               Model model) {
        try {
            userService.registerUser(username, password, email, null);
            return "redirect:/login?registered=true";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/change-password")
    public String showChangePasswordForm() {
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        try {
            userService.changePassword(username, oldPassword, newPassword);
            model.addAttribute("message", "Password changed successfully!");
            return "change-password";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "change-password";
        }
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/sprites/pokeos/{number}")
    public ResponseEntity<Void> pokeosSprite(@PathVariable int number) {
        String fallback = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + number + ".png";
        String pageUrl = "https://www.pokeos.com/pokedex/" + number;
        try {
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(8))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .timeout(Duration.ofSeconds(12))
                    .uri(URI.create(pageUrl))
                    .header("User-Agent", "Mozilla/5.0")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body() == null ? "" : response.body();

            String spriteUrl = extractMetaImage(body, "og:image");
            if (spriteUrl == null) spriteUrl = extractMetaImage(body, "twitter:image");
            if (spriteUrl == null) spriteUrl = extractFirstImageUrl(body);
            if (spriteUrl == null) spriteUrl = fallback;
            if (spriteUrl.startsWith("/")) spriteUrl = "https://www.pokeos.com" + spriteUrl;

            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(spriteUrl));
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        } catch (Exception e) {
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(fallback));
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        }
    }

    private String extractMetaImage(String html, String property) {
        Pattern p = Pattern.compile("<meta[^>]+(?:property|name)=[\"']" + Pattern.quote(property) + "[\"'][^>]+content=[\"']([^\"']+)[\"'][^>]*>", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(html);
        if (m.find()) return m.group(1);
        return null;
    }

    private String extractFirstImageUrl(String html) {
        Pattern p = Pattern.compile("https?://[^\\s\"']+\\.(?:png|jpg|jpeg|webp|gif)(?:\\?[^\\s\"']*)?", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(html);
        if (m.find()) return m.group(0);
        return null;
    }
}
