package kafka

import (
	"auth-service/internal/model"
	"context"
	"encoding/json"
	"fmt"

	"github.com/segmentio/kafka-go"
)

type Producer struct {
	writer *kafka.Writer
}

type EventProducer interface {
	Send(ctx context.Context, event model.UserRegisteredEvent) error
}

func NewProducer(broker, topic string) *Producer {
	return &Producer{
		writer: &kafka.Writer{
			Addr:                   kafka.TCP(broker),
			Topic:                  topic,
			Balancer:               &kafka.LeastBytes{},
			AllowAutoTopicCreation: true,
		},
	}
}

func (p *Producer) Close() error {
	return p.writer.Close()
}

func (p *Producer) Send(ctx context.Context, event model.UserRegisteredEvent) error {
	payload, err := json.Marshal(event)
	if err != nil {
		return fmt.Errorf("failed to marshal event: %w", err)
	}

	msg := kafka.Message{
		Key:   []byte(event.UserID.String()),
		Value: payload,
		Headers: []kafka.Header{
			{
				Key:   "eventType",
				Value: []byte("userCreated"),
			},
			{
				Key:   "content-type",
				Value: []byte("application/json"),
			},
		},
	}

	return p.writer.WriteMessages(ctx, msg)
}
