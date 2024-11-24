package com.codedifferently.lesson26.web;

import com.codedifferently.lesson26.library.Librarian;
import com.codedifferently.lesson26.library.Library;
import com.codedifferently.lesson26.library.MediaItem;
import com.codedifferently.lesson26.library.search.SearchCriteria;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/items")
@CrossOrigin
public class MediaItemsController {
  private final Library library;
  private final Librarian librarian;

  public MediaItemsController(Library library) throws IOException {
    this.library = library;
    this.librarian = library.getLibrarians().stream().findFirst().orElseThrow();
  }

  @GetMapping()
  public ResponseEntity<GetMediaItemsResponse> getItemById() {
    Set<MediaItem> items = library.search(SearchCriteria.builder().build());
    if (items.isEmpty()) {
      ResponseEntity.noContent();
    }
    List<MediaItemResponse> responseItems = items.stream().map(MediaItemResponse::from).toList();
    var response = GetMediaItemsResponse.builder().items(responseItems).build();
    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "/{id}")
  public ResponseEntity<MediaItemResponse> getItemById(@PathVariable("id") UUID id) {

    System.out.println(id.toString());

    Set<MediaItem> items = library.search(SearchCriteria.builder().id(id.toString()).build());
    Optional<MediaItem> matchedItem =
        items.stream().filter(item -> item.getId().equals(id)).findFirst();
    System.out.println("items");
    System.out.println(items);
    System.out.println(matchedItem);
    System.out.println("mathcedItems");
    return matchedItem
        .map(
            item -> {
              MediaItemResponse responseItem = MediaItemResponse.from(item);
              return ResponseEntity.ok(responseItem);
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping()
  public ResponseEntity<?> postItems(@RequestBody(required = true) MediaItemRequest request) {
    MediaItem newMediaItem = MediaItemRequest.asMediaItem(request);

    if (newMediaItem.getTitle() == null
        || newMediaItem.getType() == null
        || request.getType() == null) {

      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(request);
    }

    library.addMediaItem(newMediaItem, librarian);

    return ResponseEntity.status(HttpStatus.CREATED).body(request);
  }

  @DeleteMapping(value = "/{id}")
  public ResponseEntity<Void> deleteItem(@PathVariable("id") UUID id) {
    Set<MediaItem> test1 = library.search(SearchCriteria.builder().id(id.toString()).build());

    Set<MediaItem> item = library.search(SearchCriteria.builder().id(id.toString()).build());
    if (item.isEmpty()) {
      ResponseEntity.notFound().build();
    } else if (!test1.contains(item.iterator().next())) {
      ResponseEntity.notFound().build();
    }
    library.removeMediaItem(id, librarian);
    // Set<MediaItem> libraryAltercation = library.search(SearchCriteria.builder().build());
    return ResponseEntity.noContent().build();
  }
}
