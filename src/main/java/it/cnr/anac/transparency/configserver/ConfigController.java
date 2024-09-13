package it.cnr.anac.transparency.configserver;

import it.cnr.anac.transparency.configserver.data.Property;
import it.cnr.anac.transparency.configserver.data.PropertyRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class ConfigController {

  private final PropertyRepository repo;

  @GetMapping("/environment")
  public ResponseEntity<List<Property>> environment() {
    return ResponseEntity.ok(repo.findAll());
  }
}
