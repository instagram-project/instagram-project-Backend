package com.gmail.insta.controller;

import com.gmail.insta.model.Message;
import com.gmail.insta.repository.MessageRepository;
import com.gmail.insta.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@RestController
public class MainController {

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    MessageService messageService;

    @Value("${upload.path}")
    private String uploadPath;

    // Получить список всех сообщений
    @GetMapping(value = "/all", produces={"application/json; charset=UTF-8"})
            ResponseEntity<List<Message>> getMessages()
    {

        List<Message> messages = messageRepository.findAll();

        return new ResponseEntity<>(messages, HttpStatus.OK);
    }

    // Получить список сообщений с пагинацией
    @GetMapping(value = {"/", "/page/{pageId}"}, produces={"application/json; charset=UTF-8"})
    ResponseEntity<Collection<Message>> getMessages(@PathVariable("pageId") Optional<Integer> pageId) {
        Collection<Message> list = messageService.findAll(pageId.orElse(1)).getContent();
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    // Получить конкретное сообщение
    @GetMapping(value = "/message/{messageId}", produces={"application/json; charset=UTF-8"})
    ResponseEntity<Message> findMessage(@PathVariable("messageId") Long messageId) {

        long id = messageId;
        Message message = messageRepository.findById(id);

        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    // Загрузка сообщения на сервер
    @PostMapping(value = "/upload", produces={"application/json; charset=UTF-8"})
    ResponseEntity<Message> uploadMessage(
            @RequestParam(value = "text", required = false) String text,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) throws IOException {
        Message message = new Message();
        message.setText(text);
        message.setDate(new Date());

        if (file != null && !file.getOriginalFilename().isEmpty()) {
            File uploadDir = new File(uploadPath);

            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }

            String uuidFile = UUID.randomUUID().toString();
            String resultFilename = uuidFile + "." + file.getOriginalFilename();

            file.transferTo(new File(uploadPath + "/" + resultFilename));

            message.setFilename(resultFilename);
        }

        messageRepository.save(message);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
